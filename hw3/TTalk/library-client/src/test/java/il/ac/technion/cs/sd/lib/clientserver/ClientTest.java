package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.lib.clientserver.Client;
import il.ac.technion.cs.sd.lib.clientserver.InvalidOperation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ClientTest {
	Client client;
	class MessageData{
		String data;
		MessageData(String msg){
			data = msg;
		}
	}
	@Before
	public void setUp() throws Exception {
		client = new Client("Idan");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBuildClient() {
		assertEquals("Idan", client.getAddress());
	}
	
	@Test(expected=InvalidOperation.class)
	public void startANewLoopTwiceThrowsException(){
		client.start("T2", x->{}, Integer.class);
		client.start("T2", x->{}, Integer.class);
	}
	
	@Test(expected=InvalidOperation.class)
	public void sendWithThrowWhenParmetric(){
		client.send("Hello");
	}
	
//	@Test
//	public void shouldntThrowWhenStopWithLoop(){
//		client.start("T2", x->{}, Integer.class);
//		client.stopListenLoop();
//	}
	
	@Test(expected=InvalidOperation.class)
	public void shouldThrowWhenStopNoLoop(){
		client.stopListenLoop();
		client.stopListenLoop();
	}
	
	@Test(expected=InvalidOperation.class)
	public void sendShouldntThrowNoParam(){
		client.send(new MessageData("Hello"));
	}


}