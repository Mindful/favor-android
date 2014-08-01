package data;

import java.util.Properties;
import java.util.Vector;

import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;

import com.favor.util.Misc;
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
	void fetch() {
		// TODO Auto-generated method stub

	}
	
	
	// todo: eventually should be private, use or not determined by settings
		public void updateEmail() {
			//TODO: this only does received right now, we'll have to hit sent as a separate folder
			Properties props = new Properties();
			props.setProperty("mail.store.protocol", "imaps");
			Debug.log("start email test");
			try {
				Session session = Session.getInstance(props, null);
				Store store = session.getStore();
				store.connect("imap.gmail.com", "joshuabtanner@gmail.com",
						"tahnqydxlonnpqco");
				IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
				inbox.open(Folder.READ_ONLY);

				//This code is largely modeled after the time I did something similar in Python, here:
				//https://github.com/Mindful/PyText/blob/master/src/pt_mail_internal.py
				
				lastFetch = getLastFetch();
				long[] uidArray = (long[])inbox.doCommand(new IMAPFolder.ProtocolCommand() {
					@Override
					public Object doCommand(IMAPProtocol p) throws ProtocolException {
						StringBuilder searchCommand = new StringBuilder("UID SEARCH ");
						String[] test = {"clifthom@evergreen.edu", "stong7@yahoo.com", "funkymystic@gmail.com"};
						for (int i = 1; i < test.length; i++) searchCommand.append("OR "); //Start i at 1 so we get one less OR
						for (int i = 0; i < test.length; i++) searchCommand.append("FROM \"").append(test[i]).append("\" ");
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
		               } else Misc.logError("Email communication failed: response not OK");	
		               return uids.toArray();
					}
				});
				
			//Have to use fully qualified namespace to avoid overlap with Favor's native "Message" object
			javax.mail.Message[] messages = inbox.getMessagesByUID(uidArray);
			for(int i = 0; i < messages.length; i++){
				messages[i].getFrom()[0].toString();
				messages[i].getContent(); //Well, this is probably what we'll need to determine both the message size 
				//and whether it had media or not, but it looks like determining that is going to take consulting the
				//documentation
				messages[i].getReceivedDate().getTime();
				boolean sent = false;
				long id = uidArray[i];
				//exportMessage(false, uidArray[i], messages[i].getFrom()[0].toString(),  )
				//messages[i].getSentDate(); //For when we write the sent message querying code
			}
				

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	@Override
	String formatAddress(String address) {
		// TODO Auto-generated method stub
		return null;
	}

}
