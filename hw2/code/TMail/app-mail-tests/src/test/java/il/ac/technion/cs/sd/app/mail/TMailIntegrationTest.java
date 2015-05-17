package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.MessengerException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TMailIntegrationTest {

	private ServerMailApplication server1 = new ServerMailApplication("server1");

	private ClientMailApplication clientOfer;
	private ClientMailApplication clientIdan;
	private ClientMailApplication clientGal;
	private ClientMailApplication clientTom;
	
	List<ClientMailApplication> clients = new LinkedList<ClientMailApplication>();	
	
	
	@Before 
	public void setup() throws InterruptedException {
		 
		// TODO: after all tests pass - try commenting out the following (and initially deleting the data directory).
		server1.clean();
		
		assertEquals(server1.getAddress(), "server1");
		 	
		clientOfer = addClient(server1, "Ofer");
		clientIdan = addClient(server1, "Idan");
		clientGal = addClient(server1, "Gal");
		clientTom = addClient(server1, "Tom");
		
		server1.start();
		Thread.sleep(100);//TODO;
	}

	private ClientMailApplication addClient(ServerMailApplication server, String clientName) {
		ClientMailApplication $ = new ClientMailApplication(server.getAddress(), clientName);
		clients.add($);
		return $;
	}
	

	private void doSomeChat() throws Exception
	{
		clientGal.sendMail("Ofer", "Bad job");
		clientOfer.sendMail("Gal", ":(");
		clientGal.sendMail("Ofer", "jk");
		clientOfer.sendMail("Gal", ":)");
		
		clientIdan.sendMail("Ofer", "sup");
		clientOfer.sendMail("Idan", "good");
		clientIdan.sendMail("Ofer", "bye");
		clientOfer.sendMail("Idan", "cya");
		
		clientTom.sendMail("Gal", "hi");
		clientGal.sendMail("Tom", "hey");
		
		clientIdan.sendMail("Tom", "whoRU");
	}
	
	
	@After 
	public void teardown() {
		server1.stop();
		server1.clean();
		
		clients.forEach(x -> x.stop());
	}
	
	@Test 
	public void basicTest() throws Exception{
		clientGal.sendMail("Ofer", "Hi");
		assertEquals(clientGal.getContacts(1), Arrays.asList("Ofer"));
		assertEquals(clientOfer.getNewMail(), Arrays.asList(new Mail("Gal", "Ofer", "Hi")));
		clientOfer.sendMail("Gal", "sup");
		assertEquals(clientGal.getAllMail(3), Arrays.asList(
				new Mail("Ofer", "Gal", "sup"),
				new Mail("Gal", "Ofer", "Hi")));
	}
	

	
	@Test
	public void getCorrespondences() throws Exception
	{
		doSomeChat();
		
		AssertCorrespondences(clientOfer, clientIdan, 1, new Mail("Ofer", "Idan", "cya"));
	}
	
	private void AssertCorrespondences(ClientMailApplication a, ClientMailApplication b,
			int howMany, Mail ... expected)
	{
		List<Mail> expectedList = Arrays.asList(expected);
		
		List<Mail> a_to_b = a.getCorrespondences(b.getUsername(), howMany);
		List<Mail> b_to_a = b.getCorrespondences(a.getUsername(), howMany);
		
		//TODO
		System.out.println(a_to_b);
		System.out.println(expectedList);
		
		assertEquals(a_to_b, expectedList);
		assertEquals(b_to_a, expectedList);
	}
	

	
}
