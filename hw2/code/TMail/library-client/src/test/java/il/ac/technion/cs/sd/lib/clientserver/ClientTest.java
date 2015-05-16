package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientTest {

	private MessageData md1;
	private MessageData md2;
	
	private String messenger1_name;
	private String messenger2_name;
	private Messenger messenger1;
	private Messenger messenger2;
	
	private String client1_name;
	private String client2_name;
	private Client client1;
	private Client client2;
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		ArrayList<String> strings1 = new ArrayList<String>();
		strings1.add("aaa");
		strings1.add("bbb");
		strings1.add("ccc");
		
		ArrayList<String> strings2 = new ArrayList<String>();
		
		
		md1 = new MessageData("messageType1", strings1);
		md2 = new MessageData("messageType2", strings2);
		

		messenger1_name = UUID.randomUUID().toString();
		messenger2_name = UUID.randomUUID().toString();
		messenger1 = new MessengerFactory().start(messenger1_name);
		messenger2 = new MessengerFactory().start(messenger2_name);
		
		client1_name = UUID.randomUUID().toString();
		client2_name = UUID.randomUUID().toString();
		client1 = new Client(client1_name);
		client2 = new Client(client1_name);
	}

	@After
	public void tearDown() throws Exception {
		messenger1.kill();
		messenger2.kill();
	}

	@Test
	public void test() throws MessengerException {
		client1.sendToServerAndGetAnswer(messenger1_name, md1);
		
		
	}

}
