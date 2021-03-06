package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientTest {


	
	private String messenger1_name;
	private String messenger2_name;
	private Messenger messenger1;
	private Messenger messenger2;
	
	private String client1_name;
	private Client client1;
	
	ArrayList<String> stringList1;
	ArrayList<String> stringList2;
	ArrayList<String> stringList3;
	
	private MessageData md1;
	
	
	boolean errorOnAnotherThread = false;
	

	@Before
	public void setUp() throws Exception {
		stringList1 = new ArrayList<String>();
		stringList1.add("aaa");
		stringList1.add("bbb");
		stringList1.add("ccc");
		
		stringList2 = new ArrayList<String>();
		stringList2.add("ddd");
		
		stringList3 = new ArrayList<String>();
		
		
		md1 = new MessageData("messageType1", stringList1);

		messenger1_name = UUID.randomUUID().toString();
		messenger2_name = UUID.randomUUID().toString();
		messenger1 = new MessengerFactory().start(messenger1_name);
		messenger2 = new MessengerFactory().start(messenger2_name);
		
		client1_name = UUID.randomUUID().toString();
		client1 = new Client(client1_name);
		
		
	}

	@After
	public void tearDownAndValidate() throws Exception {
		assertFalse(errorOnAnotherThread);
		messenger1.kill();
		messenger2.kill();
	}

	
	
	@SuppressWarnings("deprecation")
	@Test(timeout=3000)
	public void sendAndGetAnswer() throws MessengerException, InterruptedException {
		
		final MessageData answer = new MessageData(
				messenger1_name, "123", stringList1);
		
		Thread thread = new Thread(() -> {
			listenForExpectedMessage(messenger1, md1);
			sendBackMessage(messenger1, client1_name, answer);
		});
		
		startThreadAndYield(thread);
		
		MessageData actualAnswer = 
				client1.sendToServerAndGetAnswer(messenger1_name, md1);
		
		thread.stop();
		
		assertTrue(messagesAreEqualIgnoringFromAddress(actualAnswer, answer));
	}
	
	
	// on failure sets errorOnAnotherThread to true and throw exception.
	private void listenForExpectedMessage(
			Messenger messenger, MessageData expected)
	{
		byte[] data = null;
		try {
			data = messenger.listen();
		} catch (Exception e) {
			errorOnAnotherThread = true;
			throw new RuntimeException();
		}
		
		MessageData md = MessageData.deserialize(data);
		if (! md.equals(expected))
		{
			errorOnAnotherThread = true;
			throw new RuntimeException();
		}
	}


	// on failure sets errorOnAnotherThread to true and throw exception.
	private void sendBackMessage(
			Messenger messenger, String clientName, MessageData md)
	{
		try {
			messenger.send(clientName, md.serialize());
		} catch (Exception e) {
			errorOnAnotherThread = true;
			return;
		}
	}
	
	
	private void startThreadAndYield(Thread thread) throws InterruptedException {
		thread.start();
		Thread.yield();
		Thread.sleep(100);
	}
	
	
	// duplication. Should move this to library common test project.
	private boolean messagesAreEqualIgnoringFromAddress (
			MessageData m1, MessageData m2)
	{
		return (m1.getMessageType().equals(m2.getMessageType())) &&
				(m1.getData().equals(m2.getData()));
	}
	
}
