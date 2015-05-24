/**
 * 
 */
package il.ac.technion.cs.sd.app.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import il.ac.technion.cs.sd.lib.clientserver.MessageData;
import il.ac.technion.cs.sd.lib.clientserver.ServerTask;

/**
 * @author idansc
 *
 */
class ServerTaskMail implements ServerTask {
	
	// The data read/written by the streams of _persistentConfig are the content
	// written/read from allMail.
	private PersistentConfig _persistentConfig;
	
	// All the lists in the following data structures are ordered from new to old.
	private LinkedList<Mail> allMail = new LinkedList<Mail>();
	private Map<String,LinkedList<Mail>> allMailsSentByPerson = new HashMap<>();
	private Map<String,LinkedList<Mail>> allMailsReceivedByPerson = new HashMap<>();
	private Map<String,LinkedList<Mail>> allMailsSentAndReceivedByPerson = new HashMap<>();
	// allMailsBetweenPeople[person1,person2] == allMailsBetweenPeople[person2,person1]
	private Map<Pair<String,String>,LinkedList<Mail>> allMailsBetweenPeople = new HashMap<>();
	private Map<String,DoublyLinkedList<Mail>> allNewMailSentToPerson = new HashMap<>();
	private Map<String,Set<String>> contactsOfPerson = new HashMap<>();
		
	
	// During this constructor 
	public ServerTaskMail(PersistentConfig persistentConfig) 
			throws IOException
	{
		_persistentConfig = persistentConfig;
	}
	
	public void loadPersistentData() throws IOException
	{
		InputStream in = _persistentConfig.getPersistentMailInputStream();
		
		List<Mail> allMails = readAllMailsFromStream(in);
		for (Mail mail : allMails)
		{
			insertMailIntoStructures(mail);
		}
		
		in.close();
	}
	
	public void savePersistentData() throws IOException
	{
		OutputStream out = 
				_persistentConfig.getPersistentMailOverwriteOutputStream();
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		Gson gson = new GsonBuilder().create();
		writer.beginArray();
		for (Mail mail : allMail)
		{
			DoublyLinkedList<Mail>.Node tmp = mail.newMailNode;
			mail.newMailNode = null;
			
			gson.toJson(mail, Mail.class, writer);
			
			mail.newMailNode = tmp;
		}	
		writer.endArray();
		writer.close();
	}
	

	private <T> LinkedList<T> ReturnValidList(LinkedList<T> l)
	{
		if (l == null)
		{
			return new LinkedList<T>();
		}
		return l;
	}
	
	private <T> Set<T> ReturnValidSet(Set<T> s)
	{
		if (s == null)
		{
			return new HashSet<T>();
		}
		return s;
	}
	
	private <T> DoublyLinkedList<T> ReturnValidDL(DoublyLinkedList<T> l)
	{
		if (l == null)
		{
			return new DoublyLinkedList<T>();
		}
		return l;
	}
	
	@Override
	public MessageData run(MessageData data) {
		
		MessageData $ = new MessageData("");
		switch (TaskType.valueOf(data.getMessageType())) {
		case SEND_MAIL_TASK: {
			Iterator<String> it = data.getData().iterator();
			String from = data.getFromAddress();
			String whom = it.next();
			String what = it.next();
			
			Mail mail = new Mail(from,whom,what);
			insertMailIntoStructures(mail);
			break;
		}
		case GET_CORRESPONDENCES_TASK: {
			String from = data.getFromAddress();
			Iterator<String> it = data.getData().iterator();
			String whom = it.next();
			int howMany = Integer.parseInt(it.next());
			
			List<Mail> mailList =  ReturnValidList(allMailsBetweenPeople.get(new Pair<String,String>(from,whom)));
			setDataToListPrefix($, howMany, mailList);
			markMailsAsRead(mailList, from);
			break;
		}
		case GET_SENT_MAILS_TASK: {
			String from = data.getFromAddress();					
			int howMany = Integer.parseInt(data.getData().get(0));
			
			List<Mail> mailList =  ReturnValidList(allMailsSentByPerson.get(from));
			setDataToListPrefix($, howMany, mailList);
			markMailsAsRead(mailList, from);
			break;
		}
		case GET_INCOMING_MAIL_TASK: {
			String from = data.getFromAddress();					
			int howMany = Integer.parseInt(data.getData().get(0));
			
			List<Mail> mailList =  ReturnValidList(allMailsReceivedByPerson.get(from));
			setDataToListPrefix($, howMany, mailList);
			markMailsAsRead(mailList, from);
			break;
		}
		case GET_ALL_MAIL_TASK: {
			String from = data.getFromAddress();					
			int howMany = Integer.parseInt(data.getData().get(0));
			
			List<Mail> mailList =  ReturnValidList(allMailsSentAndReceivedByPerson.get(from));
			setDataToListPrefix($, howMany, mailList);
			markMailsAsRead(mailList, from);
			break;
		}
		case GET_NEW_MAIL_TASK:
		{
			String from = data.getFromAddress();
			DoublyLinkedList<Mail> mailList = ReturnValidDL(allNewMailSentToPerson.get(from));
			$.setData(fromMailListToStringList(mailList));
			LinkedList<Mail> tmp = new LinkedList<Mail>();
			mailList.forEach(x -> tmp.add(x));
			markMailsAsRead(tmp, from);
			break;
		}
		case GET_CONTACTS_TASK: {
			String from = data.getFromAddress();					
			List<String> contactsList = new LinkedList<>();
			contactsList.addAll(ReturnValidSet(contactsOfPerson.get(from)));
			Collections.sort(contactsList);
			$.setData(contactsList);			
			break;
		}
		default:
			throw new RuntimeException("Task types unknown!");
		}
		return $;

	}

	private void setDataToListPrefix(MessageData $, int prefixLength,
			List<Mail> mailList) {
		List<Mail> prefix = mailList.subList(0, Math.min(prefixLength,mailList.size()));
		$.setData(fromMailListToStringList(prefix));
	}


	/**
	 * @param mailList
	 */
	private List<String> fromMailListToStringList(Iterable<Mail> mailList) {
		
		List<String> $ = new LinkedList<String>();
		for(Mail m : mailList){
			$.add(m.from); 
			$.add(m.to);
			$.add(m.content);
		}
		return $;
	}
	
	
	private void insertMailIntoStructures(Mail mail)
	{
		
		allMail.addFirst(mail);

		insertMailIntoLinkedListInMap(allMailsSentByPerson,mail.from,mail);
		
		insertMailIntoLinkedListInMap(allMailsReceivedByPerson,mail.to,mail);

		insertMailIntoLinkedListInMap(allMailsSentAndReceivedByPerson,mail.to,mail);
		if (mail.to != mail.from)
			insertMailIntoLinkedListInMap(allMailsSentAndReceivedByPerson,mail.from,mail);
		
		Pair<String,String> pair1 = new Pair<>(mail.from,mail.to);
		Pair<String,String> pair2 = new Pair<>(mail.to,mail.from);
		insertMailIntoLinkedListInMap(allMailsBetweenPeople, pair1, mail);
		if (mail.to != mail.from)
			insertMailIntoLinkedListInMap(allMailsBetweenPeople, pair2, mail);
			
		if (!mail.alreadyRead)
		{
			assert (mail.newMailNode == null);
			mail.newMailNode = 
				insertMailIntoDoublyLinkedListInMap(
						allNewMailSentToPerson,mail.to,mail); 
		}
		
		insertStringToSetInMap(contactsOfPerson, mail.from, mail.to);
		insertStringToSetInMap(contactsOfPerson, mail.to, mail.from);

	}


	private void insertStringToSetInMap(
			Map<String,Set<String>> map, String key, String newStr) {
		Set<String> contacts = map.get(key);
		if (contacts == null)
		{
			contacts = new HashSet<String>();
			map.put(key, contacts);
		}
		contacts.add(newStr);
	}
	
	// Insert to the beginning of the list, which is value matching 'key' in map.
	private <T> void insertMailIntoLinkedListInMap (
			Map<T,LinkedList<Mail>> map, T key, Mail mail)
	{
		LinkedList<Mail> mailsList = map.get(key);
		if (mailsList == null)
		{
			mailsList = new LinkedList<Mail>();
			map.put(key, mailsList);
		}
		mailsList.addFirst(mail);
	}
	
	// Insert to the beginning of the list, which is value matching 'key' in map.
	// returns the node of the new added element in the list.
	private <T> DoublyLinkedList<Mail>.Node insertMailIntoDoublyLinkedListInMap (
			Map<T,DoublyLinkedList<Mail>> map, T key, Mail mail)
	{ 
		DoublyLinkedList<Mail> mailsList = map.get(key);
		if (mailsList == null)
		{
			mailsList = new DoublyLinkedList<Mail>();
			map.put(key, mailsList);
		}
		return mailsList.addFirst(mail);
	}
	
	
	private LinkedList<Mail> readAllMailsFromStream(InputStream stream) throws IOException
	{
		Gson gson = new GsonBuilder().create();
		JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
		
		
		LinkedList<Mail> $ = new LinkedList<Mail>();
		reader.beginArray();
		while (reader.hasNext()) {
            Mail mail = gson.fromJson(reader, Mail.class);
            $.add(mail);
        }
		
		reader.endArray();
        reader.close();
		
		return $;
		
	}
	
	
	
	// marks the mails in the list as read but, but only those that were sent 
	// to receiver.
	private void markMailsAsRead(Iterable<Mail> mails, String receiver)
	{
		for (Mail m : mails)
		{
			markMailAsRead(m,receiver);
		}
	}
	
	// marks m as read but only if it was sent to receiver.
	private void markMailAsRead(Mail m, String receiver)
	{
		if (m.alreadyRead)
		{
			assert(m.newMailNode == null);
			return;
		}
		
		if (!m.to.equals(receiver))
			return;
		
		
		m.alreadyRead = true;
		allNewMailSentToPerson.get(m.to).removeNode(m.newMailNode);
		m.newMailNode = null;
		
		
	}


	
	
}
