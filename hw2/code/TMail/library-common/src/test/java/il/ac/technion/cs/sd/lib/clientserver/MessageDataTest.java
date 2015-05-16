package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MessageDataTest {

	private MessageData md1;
	ArrayList<String> stringList1;
	

	@Before
	public void setUp() throws Exception {
		
		stringList1 = new ArrayList<String>();
		stringList1.add("aaa");
		stringList1.add("bbb");
		stringList1.add("ccc");
		
		md1 = new MessageData("address1", "messageType1", stringList1);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void serializeAndDeserialize() {
		MessageData md = MessageData.deserialize(md1.serialize());
		assertEquals(md,md1);
		md.getData().add("123");
		assertNotEquals(md, md1);
	}

}
