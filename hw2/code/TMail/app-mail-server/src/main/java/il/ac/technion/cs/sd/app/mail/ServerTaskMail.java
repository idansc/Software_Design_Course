/**
 * 
 */
package il.ac.technion.cs.sd.app.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
import il.ac.technion.cs.sd.msg.Messenger;

/**
 * @author idansc
 *
 */
class ServerTaskMail implements ServerTask {
	
	
	// key = name of 
	private Map<String,List<Mail>> allMailsSentByPerson = new HashMap<>();
	private Map<String,List<Mail>> allMailsReceivedByPerson = new HashMap<>();
	private Map<String,List<Mail>> allMailsSentAndReceivedByPerson = new HashMap<>();
	// allMailsBetweenPeople[person1,person2] == allMailsBetweenPeople[person2,person1]
	private Map<Pair<String,String>,List<Mail>> allMailsBetweenPeople = new HashMap<>();
	private Map<String,List<Mail>> allNewMailSentToPerson = new HashMap<>();
	private Map<String,Set<String>> contactsOfPerson = new HashMap<>();
	
	private String mailsFileFilename;
	
	public ServerTaskMail(String serverAddress) throws IOException
	{
		mailsFileFilename = getClass().getResource(serverAddress + ".txt").toString();
		ReadAndLoadToServerAllMailsFromFile(mailsFileFilename);
	}
	

	enum FileType {
		IN_FILE,
		OUT_FILE
	}
	/* (non-Javadoc)
	 * @see il.ac.technion.cs.sd.lib.clientserver.ServerTask#run(il.ac.technion.cs.sd.msg.Messenger, il.ac.technion.cs.sd.lib.clientserver.MessageData)
	 */
	@Override
	public MessageData run(MessageData data) {
		
		MessageData $ = new MessageData("");
		
		switch (TaskType.valueOf(data.getMessageType())) {
		case SEND_MAIL_TASK:
			Iterator<String> it = data.getData().iterator();
			String from = data.getFromAddress();
			String whom = it.next();
			String what = it.next();
			
			Mail mail = new Mail(from,whom,what);
			insertMailIntoStructures(mail);
			try {
				appendMailToFile(mail, mailsFileFilename);
			} catch (IOException e) {
				//TODO: unreported failure.
			}
			return $;

		default:
			break;
		}

	}
	
	
	
	private void ReadAndLoadToServerAllMailsFromFile(String filename) throws IOException
	{
		ArrayList<Mail> allMails = readAllMailsFromFile(filename);
		for (Mail mail : allMails)
		{
			insertMailIntoStructures(mail);
		}
	}
	
	private void insertMailIntoStructures(Mail mail)
	{

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
	
	private ArrayList<Mail> readAllMailsFromFile(String filename) throws IOException
	{
		File file = new File(filename);
		return readAllMailsFromStream(new FileInputStream(file));
	}
	
	private ArrayList<Mail> readAllMailsFromStream(InputStream stream) throws IOException
	{
		Gson gson = new GsonBuilder().create();
		JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
		
		
		ArrayList<Mail> $ = new ArrayList<Mail>();
		reader.beginArray();
		while (reader.hasNext()) {
            Mail mail = gson.fromJson(reader, Mail.class);
            $.add(mail);
        }
		
		reader.endArray();
        reader.close();
		
		return $;
		
	}
	
	/* mail will be appended to outputFile */
	private void appendMailToFile(Mail mail, String outputFile) throws IOException
	{
		File file = new File(outputFile);
		OutputStream stream = new FileOutputStream(file, true);
		writeMailToStream(mail,stream);
		stream.flush();
		stream.close();
		
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
