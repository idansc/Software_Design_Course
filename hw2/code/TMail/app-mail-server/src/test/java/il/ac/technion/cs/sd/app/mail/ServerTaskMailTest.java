package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.lib.clientserver.MessageData;

import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerTaskMailTest {

	private List<Mail> _mailList;
	ServerTaskMail _task1;
	
	
	// Use buffers instead of disk.
	private class PersistentConfigWithBuffers implements PersistentConfig {
		@Override
		public InputStream getPersistentMailInputStream() throws IOException {
			return null; //TODO
		}
		
		@Override
		public OutputStream getPersistentMailOverwriteOutputStream() throws FileNotFoundException {
			return null; //TODO
		}
	}
	
	
	@Before
	public void setUp() throws Exception {
		List<String> usernames = Arrays.asList("Idan","Ofer","Gal");
		_mailList = new ArrayList<Mail>();
		_mailList.add(new Mail("Idan","Gal","Assignment 2 delay"));
		_mailList.add(new Mail("Gal","Idan","Assignment 2 delayed"));
		_mailList.add(new Mail("Ofer","Idan","We have more time"));
		_mailList.add(new Mail("Ofer","Idan","I completed the lib"));
		_mailList.add(new Mail("Gal","OFer","WOW such Assignment solution"));
		
		_task1 = new ServerTaskMail(new PersistentConfigWithBuffers());
	} 


	@After
	public void tearDown() throws Exception {
		
	}
	@Test
	public void testSendMail() {
		
		MessageData request = new MessageData(TaskType.SEND_MAIL_TASK.toString());
		List<String> stringList = new LinkedList<String>();
		stringList.add("hello");
		stringList.add("Ofer");
		stringList.add("Idan");
		request.setData(stringList);
		
		MessageData answer;
		answer = _task1.run(request)
		
		assertEquals(answer, expected)
	}

}
