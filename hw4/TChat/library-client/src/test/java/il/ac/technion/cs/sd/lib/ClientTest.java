package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.lib.ClientLib;
import il.ac.technion.cs.sd.lib.InvalidOperation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ClientTest {
	ClientLib client;
	class MessageData{
		String data;
		MessageData(String msg){
			data = msg;
		}
	}
	@Before
	public void setUp() throws Exception {
		client = new ClientLib("Idan");
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
		client.blockingSend("Hello");
	}
	
//	@Test
//	public void shouldntThrowWhenStopWithLoop(){
//		client.start("T2", x->{}, Integer.class);
//		client.stopListenLoop();
//	}
	
	@Test(expected=InvalidOperation.class)
	public void shouldThrowWhenStopNoLoop(){
		client.kill();
		client.kill();
	}
	
	@Test(expected=InvalidOperation.class)
	public void sendShouldntThrowNoParam(){
		client.blockingSend(new MessageData("Hello"));
	}


}