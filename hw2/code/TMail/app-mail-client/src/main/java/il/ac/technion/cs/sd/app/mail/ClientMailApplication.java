package il.ac.technion.cs.sd.app.mail;

import il.ac.technion.cs.sd.lib.clientserver.Client;
import il.ac.technion.cs.sd.lib.clientserver.MessageData;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.app.mail.TaskType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The client side of the TMail application.
 * Allows sending and getting mail to and from other clients using a server.
 * <br>
 * You should implement all the methods in this class 
 */
public class ClientMailApplication {
	
	private Client _client;	
	private String _serverAddress;
	
	/**
	 * Creates a new application, tied to a single user
	 * @param serverAddress The address of the server to connect to for sending and requesting mail
	 * @param username The user that will be sending and accepting the mail using this object
	 */
	public ClientMailApplication(String serverAddress, String username) {
		_client = new Client(username);
		_serverAddress = serverAddress;
	}
	
	/**
	 * Sends a mail to another user
	 * @param whom The recipient of the mail
	 * @param what The message to send
	 * @throws MessengerException 
	 */
	public void sendMail(String whom, String what) throws MessengerException {
		MessageData md = new MessageData(TaskType.SEND_MAIL_TASK.toString());
		ArrayList<String> data = new ArrayList<String>();
		data.add(whom);
		data.add(what);
		md.setData(data);
		_client.sendToServerAndGetAnswer(_serverAddress,md);
		
	}
	
	/**
	 * Get all mail sent from or to another client
	 * @param whom The other user that sent or received mail from the current user
	 * @param howMany how many mails to retrieve; mails are ordered by time of arrival to server
	 * @return A list, ordered of all mails matching the criteria, ordered by time of arrival, of size n <i>at most</i>  
	 */
	public List<Mail> getCorrespondences(String whom, int howMany){
		MessageData md = new MessageData(TaskType.GET_CORRESPONDENCES_TASK.toString());
		ArrayList<String> data = new ArrayList<String>();
		data.add(whom);
		data.add(Integer.toString(howMany));
		md.setData(data);
		MessageData result;
		result = _client.sendToServerAndGetAnswer(_serverAddress,md);
		List<Mail> $ = new ArrayList<Mail>();
		Iterator<String> iter = result.getData().iterator();
		while(--howMany!=0&&iter.hasNext()){
			$.add(new Mail(iter.next(),iter.next(),iter.next()));
		}
		return $;
	}
	
	/**
	 * Get all mail sent <b>by</b> the current user
	 * @param howMany how many mails to retrieve; mails are ordered by time of arrival to server
	 * @return A list, ordered of all mails matching the criteria, ordered by time of arrival, of size n <i>at most</i>  
	 */
	public List<Mail> getSentMails(int howMany){
		MessageData md = new MessageData(TaskType.GET_SENT_MAILS_TASK.toString());
		ArrayList<String> data = new ArrayList<String>();
		data.add(Integer.toString(howMany));
		md.setData(data);
		MessageData result;
		result = _client.sendToServerAndGetAnswer(_serverAddress,md);
		List<Mail> $ = new ArrayList<Mail>();
		Iterator<String> iter = result.getData().iterator();
		while(--howMany!=0&&iter.hasNext()){
			$.add(new Mail(iter.next(),iter.next(),iter.next()));
		}
		return $;
	}
	
	/**
	 * Get all sent <b>to</b> the current user
	 * @param howMany how many mails to retrieve; mails are ordered by time of arrival to server
	 * @return A list, ordered of all mails matching the criteria, ordered by time of arrival, of size n <i>at most</i>  
	 */
	public List<Mail> getIncomingMail(int howMany) {
		//TODO:: Code resemblance..
		MessageData md = new MessageData(TaskType.GET_INCOMING_MAIL_TASK.toString());
		ArrayList<String> data = new ArrayList<String>();
		data.add(Integer.toString(howMany));
		md.setData(data);
		MessageData result;
		result = _client.sendToServerAndGetAnswer(_serverAddress,md);
		List<Mail> $ = new ArrayList<Mail>();
		Iterator<String> iter = result.getData().iterator();
		while(--howMany!=0&&iter.hasNext()){
			$.add(new Mail(iter.next(),iter.next(),iter.next()));
		}
		return $;
	}
	
	/**
	 * Get all sent <b>to</b> or <b>by</b> the current user
	 * @param howMany how many mails to retrieve; mails are ordered by time of arrival to server
	 * @return A list, ordered of all mails matching the criteria, ordered by time of arrival, of size n <i>at most</i>  
	 */
	public List<Mail> getAllMail(int howMany) {
		//TODO:: Code resemblance..
		MessageData md = new MessageData(TaskType.GET_ALL_MAIL_TASK.toString());
		ArrayList<String> data = new ArrayList<String>();
		data.add(Integer.toString(howMany));
		md.setData(data);
		MessageData result;
		result = _client.sendToServerAndGetAnswer(_serverAddress,md);
		List<Mail> $ = new ArrayList<Mail>();
		Iterator<String> iter = result.getData().iterator();
		while(--howMany!=0&&iter.hasNext()){
			$.add(new Mail(iter.next(),iter.next(),iter.next()));
		}
		return $;
	}
	
	/**
	 * Get all sent <b>to</b> the current user that wasn't retrieved by any method yet (including this method)
	 * @return A list, ordered of all mails matching the criteria, ordered by time of arrival  
	 */
	public List<Mail> getNewMail() {
		//TODO:: Code resemblance..
		MessageData md = new MessageData(TaskType.GET_NEW_MAIL_TASK.toString());
		MessageData result;
		result = _client.sendToServerAndGetAnswer(_serverAddress,md);
		List<Mail> $ = new ArrayList<Mail>();
		Iterator<String> iter = result.getData().iterator();
		while(iter.hasNext()){
			$.add(new Mail(iter.next(),iter.next(),iter.next()));
		}
		return $;
	}
	
	/**
	 * @return A list, ordered alphabetically, of all other users that sent or received mail from the current user
	 * NOTE BY TA: WE CAN IGNORE 'howMany'  
	 */
	public List<String> getContacts(int howMany) {
		MessageData md = new MessageData(TaskType.GET_CONTACTS_TASK.toString());
		ArrayList<String> data = new ArrayList<String>();
		data.add(Integer.toString(howMany));//ignored
		md.setData(data);
		MessageData result;
		result = _client.sendToServerAndGetAnswer(_serverAddress,md);
		return result.getData();
	}
	
	/**
	 * A stopped client does not use any system resources (e.g., messengers).
	 * This is mainly used to clean resource use in test cleanup code.
	 */
	public void stop() {
		// nothing needs to be done.
	}
}
