package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerFactory;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientTest {

	private MessageData md1;
	private MessageData md2;
	private Messenger messenger1;
	private Messenger messenger2;
	
	
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
		
		messenger1 = MessengerFactory
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		MessageData md1 = 
		
		Client c = new Client("address1");
		
	}

}
