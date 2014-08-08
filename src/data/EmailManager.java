package data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import com.favor.util.Logger;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

import javax.mail.BodyPart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static data.DataConstants.*;


public class EmailManager extends MessageManager {
	
	private static final String SENT_UID = "lastSentUID";
	private static final String RECEIVED_UID = "lastReceivedUID";
	private static final String SENT_UID_VALIDITY = "sentUIDValidity";
	private static final String RECEIVED_UID_VALIDITY = "receivedUIDValidity";
	

	protected EmailManager(DataHandler dh) {
		super(Type.TYPE_EMAIL, "email", dh, new String[] {SENT_UID, RECEIVED_UID, SENT_UID_VALIDITY, RECEIVED_UID_VALIDITY});
	}
	
	
	@Override
	protected String sentTableEndingStatement(){
		return "PRIMARY KEY ("+KEY_ID+ "," + KEY_ADDRESS + ")";
	}

	@Override
	long fetch() {
		long count = 0;
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");
		Session session = Session.getInstance(props, null);
		Store store = null;
		String host = "imap.gmail.com"; //TODO: we should be getting these (and password, but we won't save that) somewhere
		String user = "joshuabtanner@gmail.com";
		Pattern sentPattern = Pattern.compile("sent", Pattern.CASE_INSENSITIVE);
		String sentFolderName = null;
		try{
			store = session.getStore();
			store.connect(host, user, "tahnqydxlonnpqco");
			//Attempt to find the "sent" folder
			Folder[] folders = store.getDefaultFolder().list("*");
			for (int i = 0; i < folders.length; i++){
				 if (sentPattern.matcher(folders[i].getName()).find()){
					 if (sentFolderName!=null){
						 throw new dataException("Competing sent folder names:\""+sentFolderName+"\"/\""+folders[i].getFullName());
					 } else sentFolderName = folders[i].getFullName();
				 }
			}
			if (sentFolderName==null){
				String folderList = "";
				for (Folder f: folders) folderList+=f.getFullName()+", ";
				folderList = folderList.substring(0, folderList.length()-2);
				throw new dataException("Could not find sent folder in folders ["+folderList+"]");
			}
		} catch (MessagingException e){
			Logger.exception("Error connecting to mail provider "+host+" as "+user, e);
			return 0;
		} catch (dataException e){
			Logger.exception("Error interfacing with mail provider "+host, e);
		}
		try {
			count += fetchFromServer(store, "INBOX", false);
			count += fetchFromServer(store, sentFolderName, true);
		} catch (MessagingException e) {
			Logger.exception("Error interfacing with mail provider "+host+" as "+user, e);
			e.printStackTrace();
		}
		
		return count;

	}
	
	
		private long fetchFromServer(Store store, String folderName, boolean sent) throws MessagingException {
			long count = 0;
			IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);
			folder.open(Folder.READ_ONLY);
			long lastUIDValidity = sent ? getLong(SENT_UID_VALIDITY, 0) : getLong(RECEIVED_UID_VALIDITY, 0);
			long UIDValidity = folder.getUIDValidity();
			if (lastUIDValidity!=0){
				if(lastUIDValidity!=UIDValidity){
					Logger.warn("UID validity value of "+folderName+" changed from "+lastUIDValidity+" to "+UIDValidity+
							". This will require a mail database rebuild");
					//This is bad news. We need to drop and rebuild our tables.
					dropTables();
				}
			}

			//The IMAP search building code is largely modeled after the time I did something similar in Python:
			//https://github.com/Mindful/PyText/blob/master/src/pt_mail_internal.py/#L184
			//Also, http://tools.ietf.org/html/rfc3501 defines UIDS as
			//"nz-number" (non-zero) numbers, so the lastSent/ReceivedUID should start at 1
			//Lastly, I have no idea why these two properly used variables generate unused warnings
			final String addressField = sent ? "TO" : "FROM";
			final long lastUID = (sent ? getLong(SENT_UID, 0) : getLong(RECEIVED_UID, 0))+1; //Adding 1 is important because UID fetch includes the lowest value you give it  
			if (lastUID >= (folder.getUIDNext()-1)) return 0; //If our last is less than or equal to the current max, we've no work to do
			
			//TODO: get addresses from somewhere reasonable
			final String[] addresses = {"clifthom@evergreen.edu", "stong7@yahoo.com", "funkymystic@gmail.com", "stevehope2@gmail.com", "jtanner2@pacbell.net"};
			long[] uidArray = (long[])folder.doCommand(new IMAPFolder.ProtocolCommand() {
				@Override
				public Object doCommand(IMAPProtocol p) throws ProtocolException {
					StringBuilder searchCommand = new StringBuilder("UID SEARCH ");
					for (int i = 1; i < addresses.length; i++) searchCommand.append("OR "); //Start i at 1 so we get one less OR
					for (int i = 0; i < addresses.length; i++) searchCommand.append(addressField+" \"").append(addresses[i]).append("\" ");
					searchCommand.append("UID ").append(lastUID).append(":*");
				   Argument args = new Argument(); //Admittedly I don't understand this as well as I could; the IMAP cmd generating code is mine
		           args.writeString("ALL"); //but the actual Javamail implementation is basically sourced from http://www.mailinglistarchive.com/javamail-interest@java.sun.com/msg00561.html
	               Response[] r = p.command(searchCommand.toString(), args);
	               Response response = r[r.length - 1];
	               ArrayList<Long> uids = new ArrayList<Long>();
	               if (response.isOK()) { 
                       for (int i = 0, len = r.length; i < len; i++) {
                           if (!(r[i] instanceof IMAPResponse)) continue;
                           IMAPResponse ir = (IMAPResponse) r[i];
                           if (ir.keyEquals("SEARCH")) {
                                   String num;
                                   while ((num = ir.readAtomString()) != null) {
                                           uids.add(Long.valueOf(num));
                                   }
                           }
                       }
	               } else Logger.error("Email communication failed with response: "+response);
	               //Obnoxiously we have to unbox each long before returning it, but if we start off with a long[] array
	               //then we risk having empty slots because the only thing we can use for its size is r.length
	               long[] ret = new long[uids.size()];
	               for (int i = 0; i < uids.size(); i++){
	            	  ret[i] =  uids.get(i).longValue();
	               }
	               
	               return ret;
				}
			});
			
		if (uidArray.length==0) return 0;
		//Have to use fully qualified namespace to avoid overlap with Favor's native "Message" object
		//Also (this has to be true for the code to work properly) getMessagesByUID returns an array of
		//messages with messages in the same spot as their UIDs in the argument array, and the initial
		//UID argument array is in ascending order.
		javax.mail.Message[] messages = folder.getMessagesByUID(uidArray);
		beginTransaction();
		try{
			for(int i = 0; i < messages.length; i++) {
				parseEmail(messages[i], uidArray[i], addresses, sent);
				++count;
			}
			if (sent) {
				putLong(SENT_UID, uidArray[uidArray.length-1]);
				putLong(SENT_UID_VALIDITY, UIDValidity);
			}
			else {
				putLong(RECEIVED_UID, uidArray[uidArray.length-1]); 
				putLong(RECEIVED_UID_VALIDITY, UIDValidity);
			}
			successfulTransaction();
		} catch (MessagingException e){
			Logger.exception("Problem importing mail", e);
		} catch (IOException e){
			Logger.exception("IO Problem importing mail", e);
		}
		endTransaction();
		return count;
	}
		
	private void parseEmail(javax.mail.Message message, long UID, String addresses[], boolean sent) throws IOException, MessagingException{
		int media = 0;
		String body = null;
		Set<String> validAddresses = new HashSet<String>(Arrays.asList(addresses));
		if(message instanceof MimeMessage)
        {
            MimeMessage m = (MimeMessage)message;
            Object contentObject = m.getContent();
            if(contentObject instanceof Multipart)
            {
                BodyPart clearTextPart = null;
                BodyPart htmlTextPart = null;
                Multipart content = (Multipart)contentObject;
                int count = content.getCount();
                for(int i=0; i<count; i++)
                {
                    BodyPart part =  content.getBodyPart(i);
                    if(part.isMimeType("text/plain")) clearTextPart = part;
                    else if (part.isMimeType("text/html")) htmlTextPart = part;
                    else media = 1; //If it's not text and it's not HTML, that's media enough for me
                }

                if (clearTextPart!=null) body = (String) clearTextPart.getContent();
                //else if (htmlTextPart!=null) body = Jsoup.parse((String) htmlTextPart.getContent()).text();
                else if (htmlTextPart!=null) body = (String) htmlTextPart.getContent();

            }
            else if (contentObject instanceof String) body = (String) contentObject; //Simple text-only email, not MIME formatted
            else Logger.error("Unable to decypher email: "+message.toString());//We're not really sure what it is now
        }
		if (body==null) body = ""; //No code to export null values
		if (sent){
	        long date = message.getSentDate().getTime();
	        Address to[] = message.getAllRecipients();
	        String addr;
	        //Generate a record for every recipient
	        for (int i = 0; i < to.length; i++){
	        	addr = ((InternetAddress)to[i]).getAddress();
	        	if (validAddresses.contains(addr)) exportMessage(true, UID, date, addr, body, media);
	        }
		} else {
	        long date = message.getReceivedDate().getTime();
        	Address from[] = message.getFrom();
        	String addr;
        	//Generate a record for the first recognized sender. Multiple senders is such a weird edge case anyway, though
        	for (int i = 0; i < from.length; i++){
        		addr = ((InternetAddress)from[i]).getAddress();
        		if (validAddresses.contains(addr)) exportMessage(false, UID, date, addr, body, media);
	        }
		}
	}
	
	@Override
	String formatAddress(String address) {
		return address; //No formatting necessary at current
	}

}
