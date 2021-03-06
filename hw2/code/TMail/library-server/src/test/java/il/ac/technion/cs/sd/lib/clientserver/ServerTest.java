package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.UUID;

import il.ac.technion.cs.sd.lib.clientserver.Server.ListenLoopAlreadyBeingDone;
import il.ac.technion.cs.sd.lib.clientserver.Server.NoCurrentListenLoop;
import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerTest {

	private ArrayList<String> stringList1;
	private ArrayList<String> stringList2;

	private MessageData md1;
	private MessageData md2;
	
	private String server1_name;
	private Server server1;
	
	private String messenger1_name;
	private String messenger2_name;
	private Messenger messenger1;
	private Messenger messenger2;
	
	private ServerTask task_1_2;
	private ServerTask task_2_1;
	
	boolean errorOnOtherThread = false;
	


	
	
	@Before
	public void setUp() throws Exception {
		
		stringList1 = new ArrayList<String>();
		stringList1.add("aaa");
		stringList1.add("bbb");
		stringList1.add("ccc");
		
		stringList2 = new ArrayList<String>();
		
		messenger1_name = UUID.randomUUID().toString();
		messenger1 = new MessengerFactory().start(messenger1_name);
		
		messenger2_name = UUID.randomUUID().toString();
		messenger2 = new MessengerFactory().start(messenger2_name);
		
		md1 = new MessageData(messenger1_name, "messageType1", stringList1);
		md2 = new MessageData(messenger2_name, "messageType2", stringList2);
		
		server1_name = UUID.randomUUID().toString();
		
		// expects to receive mk1. Sends back to the client md2.
		task_1_2 = new ServerTask() {
			public MessageData run(MessageData data) {
				if (! messagesAreEqualIgnoringFromAddress(md1,data))
					errorOnOtherThread = true;
				return md2;
			}
		};
		
		// expects to receive mk2. Sends back to the client md1.
		task_2_1 = new ServerTask() {
			public MessageData run(MessageData data) {
				if (! md2.getData().equals(data.getData()))
					errorOnOtherThread = true;
				return md1;
			}
		};
		
		
		server1 = new Server(server1_name);
	}

	@After
	public void tearDownAndValidate() throws Exception {
		assertFalse(errorOnOtherThread);
		messenger1.kill();
		messenger2.kill();
	}

	@Test (timeout=3000)
	public void answerSingleClient() throws MessengerException, InterruptedException {
		
		startListenLoopAndYield(server1, task_1_2);
		
		messenger1.send(server1_name, md1.serialize());
		
		readMessageAndAssert(messenger1, md2);
		
		server1.stopListenLoop();		
	}
	
	
	@Test (timeout=3000)
	public void answerMultipleClients() throws MessengerException, InterruptedException {
		
		startListenLoopAndYield(server1, task_2_1);
		
		md2.setFromAddress(messenger1_name);
		messenger1.send(server1_name, md2.serialize());
		
		md2.setFromAddress(messenger2_name);
		messenger2.send(server1_name, md2.serialize());
		
		readMessageAndAssert(messenger1, md1);
		readMessageAndAssert(messenger2, md1);
		
		server1.stopListenLoop();		
	}
	
	
	@Test (timeout=10000)
	public void startMultipleSequentialListenLoops() throws MessengerException, InterruptedException {
		
		for (int i=0; i<3; i++)
		{
			startListenLoopAndYield(server1, task_2_1);
			md2.setFromAddress(messenger1_name);
			messenger1.send(server1_name, md2.serialize());
			
			readMessageAndAssert(messenger1, md1);
			
			server1.stopListenLoop();
		}
	}
	
	@Test (expected=ListenLoopAlreadyBeingDone.class, timeout=1000)
	public void startMultipleOverlappingListenLoops() throws MessengerException, InterruptedException {
		startListenLoopAndYield(server1, task_1_2);
		startListenLoopAndYield(server1, task_2_1);
	}

	
	@Test (expected=NoCurrentListenLoop.class, timeout=1000)
	public void endNonExistingLoop() throws MessengerException, InterruptedException {
		server1.stopListenLoop();
	}


	private void readMessageAndAssert(
			Messenger messenger, MessageData expectedMessage) 
			throws MessengerException {
		
		MessageData md = MessageData.deserialize(messenger.listen());
		assertTrue(messagesAreEqualIgnoringFromAddress(md,expectedMessage));
	}
	
	private boolean messagesAreEqualIgnoringFromAddress (
			MessageData m1, MessageData m2)
	{
		return (m1.getMessageType().equals(m2.getMessageType())) &&
				(m1.getData().equals(m2.getData()));
	}

	private void startListenLoopAndYield(Server server, ServerTask task) 
			throws InterruptedException {
		server.startListenLoop(task);
		Thread.yield();
		Thread.sleep(100);
	}

}
