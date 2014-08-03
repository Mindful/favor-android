package data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import com.favor.util.Logger;
import com.sun.mail.iap.Argument;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;

import javax.mail.Address;
import javax.mail.BodyPart;
//import javax.mail.Message;
import javax.mail.search.SearchTerm;

import static data.DataConstants.*;

public class EmailManager extends MessageManager {

	protected EmailManager(DataHandler dh) {
		super(Type.TYPE_EMAIL, "email", dh);
		//http://tools.ietf.org/html/rfc3501 defines UIDS as
		//"nz-number" (non-zero) numbers, so the lastFetch should start at 1
	}
	
	
	//TODO: 95% sure this is necessary, but can always do with some verification
	@Override
	protected String sentTableEndingStatement(){
		return "PRIMARY KEY ("+KEY_ID+ "," + KEY_ADDRESS + ")";
	}

	
	long fetch(){return 0;}
	//@Override
	public long fetchTest() {
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imaps");
		Debug.log("start email test");
		Session session = Session.getInstance(props, null);
		Store store = null;
		String host = "imap.gmail.com"; //TODO: we should be getting these (and password, but we won't save that) somewhere
		String user = "joshuabtanner@gmail.com";
		Pattern sentPattern = Pattern.compile("sent", Pattern.CASE_INSENSITIVE);
		try{
			store = session.getStore();
			store.connect(host, user, "tahnqydxlonnpqco");
			//Attempt to find the "sent" folder
			String sentFolderName = null;
			Folder[] folders = store.getDefaultFolder().list("*");
			for (int i = 0; i < folders.length; i++){
				 Debug.log(">> "+folders[i].getName());
				 if (sentPattern.matcher(folders[i].getName()).find()){
					 Debug.log("SentFolder match: "+folders[i].getFullName());
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
			fetchFromServer(store, false, "INBOX");
			//fetchFromServer(store, true);
		} catch (MessagingException e) {
			Logger.exception("Error interfacing with mail provider "+host+" as "+user, e);
			e.printStackTrace();
		}
		
		return 0;

	}
	
	
	// TODO: eventually should be private, use or not determined by settings
		private void fetchFromServer(Store store, boolean sent, String folderName) throws MessagingException {
				IMAPFolder folder =  (IMAPFolder) store.getFolder(folderName);
				folder.open(Folder.READ_ONLY);

				//The IMAP search building code is largely modeled after the time I did something similar in Python:
				//https://github.com/Mindful/PyText/blob/master/src/pt_mail_internal.py
				final String addressField = sent ? "TO" : "FROM";
				final long lastUID = sent ? getLong("lastSentUID", 1) : getLong("lastReceivedUID", 1);
				long[] uidArray = (long[])folder.doCommand(new IMAPFolder.ProtocolCommand() {
					@Override
					public Object doCommand(IMAPProtocol p) throws ProtocolException {
						StringBuilder searchCommand = new StringBuilder("UID SEARCH ");
						String[] test = {"clifthom@evergreen.edu", "stong7@yahoo.com", "funkymystic@gmail.com"};
						for (int i = 1; i < test.length; i++) searchCommand.append("OR "); //Start i at 1 so we get one less OR
						for (int i = 0; i < test.length; i++) searchCommand.append(addressField+" \"").append(test[i]).append("\" ");
						searchCommand.append("UID ").append(lastUID).append(":*");
						Debug.log(searchCommand.toString());
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
                                               Debug.log(num);
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
				
			//Have to use fully qualified namespace to avoid overlap with Favor's native "Message" object
			javax.mail.Message[] messages = folder.getMessagesByUID(uidArray);
			for(int i = 0; i < messages.length; i++){
				messages[i].getFrom()[0].toString();
				try {
					messages[i].getContent();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //Well, this is probably what we'll need to determine both the message size 
				//and whether it had media or not, but it looks like determining that is going to take consulting the
				//documentation
				messages[i].getReceivedDate().getTime();
				boolean messageSent = false;
				long id = uidArray[i];
				//exportMessage(false, uidArray[i], messages[i].getFrom()[0].toString(),  )
				//messages[i].getSentDate(); //For when we write the sent message querying code
			}
				
		}

	@Override
	String formatAddress(String address) {
		// TODO Auto-generated method stub
		return null;
	}

}
