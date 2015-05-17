/**
 * 
 */
package il.ac.technion.cs.sd.app.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
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
	
	private PersistentConfig _persistentConfig;
	
	// key = name of 
	private List<Mail> allMail = new LinkedList<Mail>();
	private Map<String,List<Mail>> allMailsSentByPerson = new HashMap<>();
	private Map<String,List<Mail>> allMailsReceivedByPerson = new HashMap<>();
	private Map<String,List<Mail>> allMailsSentAndReceivedByPerson = new HashMap<>();
	// allMailsBetweenPeople[person1,person2] == allMailsBetweenPeople[person2,person1]
	private Map<Pair<String,String>,List<Mail>> allMailsBetweenPeople = new HashMap<>();
	private Map<String,List<Mail>> allNewMailSentToPerson = new HashMap<>();
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
	}
	
	public void savePersistentData() throws IOException
	{
		OutputStream out = 
				_persistentConfig.getPersistentMailOverwriteOutputStream();
		
		for (Mail mail : allMail)
		{
			writeMailToStream(mail, out);
		}	
	}
	

	/* (non-Javadoc)
	 * @see il.ac.technion.cs.sd.lib.clientserver.ServerTask#run(il.ac.technion.cs.sd.msg.Messenger, il.ac.technion.cs.sd.lib.clientserver.MessageData)
	 */
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
			
			List<Mail> mailList =  allMailsBetweenPeople.get(new Pair<String,String>(from,whom));
			$.setData(fromMailListToStringList(mailList.subList(0, howMany-1)));			
			break;
		}
		case GET_SENT_MAILS_TASK: {
			String from = data.getFromAddress();					
			int howMany = Integer.parseInt(data.getData().get(0));
			
			List<Mail> mailList =  allMailsSentByPerson.get(from);
			$.setData(fromMailListToStringList(mailList.subList(0, howMany-1)));			
			break;
		}
		case GET_INCOMING_MAIL_TASK: {
			String from = data.getFromAddress();					
			int howMany = Integer.parseInt(data.getData().get(0));
			
			List<Mail> mailList =  allMailsReceivedByPerson.get(from);
			$.setData(fromMailListToStringList(mailList.subList(0, howMany-1)));			
			break;
		}
		case GET_ALL_MAIL_TASK: {
			String from = data.getFromAddress();					
			int howMany = Integer.parseInt(data.getData().get(0));
			
			List<Mail> mailList =  allMailsSentAndReceivedByPerson.get(from);
			$.setData(fromMailListToStringList(mailList.subList(0, howMany-1)));			
			break;
		}
		case GET_NEW_MAIL_TASK:
		{
			throw new UnsupportedOperationException("Not implemented");
		}
		case GET_CONTACTS_TASK: {
			String from = data.getFromAddress();					
			
			Set<String> mailSet =  contactsOfPerson.get(from);
			List<String> mailList = Arrays.asList(mailSet.toArray(new String[mailSet.size()]));
			Collections.sort(mailList);
			$.setData(mailList);			
			break;
		}
		default:
			break;
		}
		return $;

	}


	/**
	 * @param mailList
	 */
	private List<String> fromMailListToStringList(List<Mail> mailList) {
		
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
		
		allMail.add(mail);

		insertNewMailToMap(allMailsSentByPerson,mail.from,mail);
		
		insertNewMailToMap(allMailsReceivedByPerson,mail.to,mail);

		insertNewMailToMap(allMailsSentAndReceivedByPerson,mail.to,mail);
		if (mail.to != mail.from)
			insertNewMailToMap(allMailsSentAndReceivedByPerson,mail.from,mail);
		
		Pair<String,String> pair1 = new Pair<>(mail.from,mail.to);
		Pair<String,String> pair2 = new Pair<>(mail.to,mail.from);
		insertNewMailToMap(allMailsBetweenPeople, pair1, mail);
		if (mail.to != mail.from)
			insertNewMailToMap(allMailsBetweenPeople, pair2, mail);
			
		if (!mail.alreadyRead)
		{
			insertNewMailToMap(allNewMailSentToPerson,mail.to,mail);
		}
		
		insertNewStringToSetInMap(contactsOfPerson, mail.from, mail.to);
		insertNewStringToSetInMap(contactsOfPerson, mail.to, mail.from);
	
	}

	private void insertNewStringToSetInMap(
			Map<String,Set<String>> map, String key, String newStr) {
		Set<String> contacts = map.get(key);
		if (contacts == null)
		{
			contacts = new HashSet<String>();
			map.put(key, contacts);
		}
		contacts.add(newStr);
	}
	
	
	private <T> void insertNewMailToMap (
			Map<T,List<Mail>> map, T key, Mail mail)
	{
		List<Mail> mailsList = map.get(key);
		if (mailsList == null)
		{
			mailsList = new LinkedList<Mail>();
			map.put(key, mailsList);
		}
		mailsList.add(mail);
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
	
	
	
	private void writeMailToStream(Mail mail, OutputStream stream) throws IOException
	{
		Gson gson = new GsonBuilder().create();
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(stream, "UTF-8"));
		writer.beginArray();
		gson.toJson(mail, Mail.class, writer);
		writer.endArray();
        writer.close();
		
	}


	
	
}
