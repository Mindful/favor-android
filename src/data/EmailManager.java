package data;

import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

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
	}
	
	
	//TODO: 95% sure this is necessary, but can always do with some verification
	@Override
	protected String sentTableEndingStatement(){
		return "PRIMARY KEY ("+KEY_ID+ "," + KEY_ADDRESS + ")";
	}

	@Override
	boolean fetch() {
		// TODO Auto-generated method stub
//		Properties props = new Properties();
//		props.setProperty("mail.store.protocol", "imaps");
//		Debug.log("start email test");
//		Session session = Session.getInstance(props, null);
//		Store store;
//		String host = "imap.gmail.com"; //TODO: we should be getting these (and password, but we won't save that) somewhere
//		String user = "joshuabtanner@gmail.com";
//		try{
//			store = session.getStore();
//			store.connect(host, user, "tahnqydxlonnpqco");
//		} catch (MessagingException e){
//			Logger.exception("Error connecting to mail provider "+host+" as "+user, e);
//			return false;
//		}
//		try {
//			fetchFromServer(store, false);
//			//fetchFromServer(store, true);
//		} catch (MessagingException e) {
//			Logger.exception("Error interfacing with mail provider "+host+" as "+user, e);
//			e.printStackTrace();
//		}
//		
		return true;

	}
	
	
	// todo: eventually should be private, use or not determined by settings
		private void fetchFromServer(Store store, boolean sent) throws MessagingException {
			//TODO: is "SENT" the proper name?
				IMAPFolder inbox = (IMAPFolder) (sent ? store.getFolder("SENT") : store.getFolder("INBOX"));
				inbox.open(Folder.READ_ONLY);

				//The IMAP search buildin gcode is largely modeled after the time I did something similar in Python:
				//https://github.com/Mindful/PyText/blob/master/src/pt_mail_internal.py
				final String addressField = sent ? "TO" : "FROM";
				lastFetch = getLastFetch();
				long[] uidArray = (long[])inbox.doCommand(new IMAPFolder.ProtocolCommand() {
					@Override
					public Object doCommand(IMAPProtocol p) throws ProtocolException {
						StringBuilder searchCommand = new StringBuilder("UID SEARCH ");
						String[] test = {"clifthom@evergreen.edu", "stong7@yahoo.com", "funkymystic@gmail.com"};
						for (int i = 1; i < test.length; i++) searchCommand.append("OR "); //Start i at 1 so we get one less OR
						for (int i = 0; i < test.length; i++) searchCommand.append(addressField+" \"").append(test[i]).append("\" ");
						searchCommand.append("UID ").append(lastFetch).append(":*");
						
					   Argument args = new Argument(); //Admittedly I don't understand this as well as I could; the IMAP cmd generating code is mine
			           args.writeString("ALL"); //but the actual Javamail implementation is basically sourced from http://www.mailinglistarchive.com/javamail-interest@java.sun.com/msg00561.html
		               Response[] r = p.command(searchCommand.toString(), args);
		               Response response = r[r.length - 1];
		               Vector<Long> uids = new Vector<Long>();
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
		               } else Logger.error("Email communication failed: response not OK");	
		               return uids.toArray();
					}
				});
				
			//Have to use fully qualified namespace to avoid overlap with Favor's native "Message" object
			javax.mail.Message[] messages = inbox.getMessagesByUID(uidArray);
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
