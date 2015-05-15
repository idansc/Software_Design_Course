package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientTest {

	private MessageData md1;
	private MessageData md2;
	
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
		
		
		md1 = new MessageData("111", strings1);
		
		
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
