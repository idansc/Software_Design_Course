package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.UUID;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerTest {

	private ArrayList<String> stringList1;
	private MessageData md1;
	
	private String server1_name;
	private Server server1;
	
	private String messenger1_name;
	private Messenger messenger1;
	
	private ServerTask task1;
	
	boolean errorOnOtherThread = false;
	


	
	
	@Before
	public void setUp() throws Exception {
		
		stringList1 = new ArrayList<String>();
		stringList1.add("aaa");
		stringList1.add("bbb");
		stringList1.add("ccc");
		
		md1 = new MessageData("messageType1", stringList1);
		
		server1_name = UUID.randomUUID().toString();
		
		// sends back to the client two answers identical to the data recived.
		task1 = new ServerTask() {
			public void run(Messenger serverMessenger, MessageData data) {
				try {
					serverMessenger.send(data.getFromAddress(), data.serialize());
					serverMessenger.send(data.getFromAddress(), data.serialize());
				} catch (MessengerException e) {
					// TODO Auto-generated catch block
					errorOnOtherThread = true;
				}
			}
		};
		
		server1 = new Server(server1_name);
		
		messenger1_name = UUID.randomUUID().toString();
		messenger1 = new MessengerFactory().start(messenger1_name);

	}

	@After
	public void tearDownAndValidate() throws Exception {
		assertFalse(errorOnOtherThread);
		messenger1.kill();
	}

	@Test (timeout=3000)
	public void emptyAnswerForSingleClient() throws MessengerException, InterruptedException {
		
		startListenLoopAndYield(server1, task1);
		
		messenger1.send(server1_name, md1.serialize());
		
//		MessageData md = MessageData.deserialize(messenger1.listen());
//		assertEquals(md,md1);
//
//		md = MessageData.deserialize(messenger1.listen());
//		assertEquals(md,md1);

		server1.stopListenLoop();
		
		
		
	}

	private void startListenLoopAndYield(Server server, ServerTask task) 
			throws InterruptedException {
		server.startListenLoop(task);
		Thread.yield();
		Thread.sleep(100);
	}

}
