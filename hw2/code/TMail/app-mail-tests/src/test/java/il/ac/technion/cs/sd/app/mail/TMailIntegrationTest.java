package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.MessengerException;

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
		 
		
		assertEquals(server1.getAddress(), "server1");
		 	
		clientOfer = addClient(server1, "Ofer");
		clientIdan = addClient(server1, "Idan");
		clientGal = addClient(server1, "Gal");
		clientTom = addClient(server1, "Tom");
		
		server1.start();
	}

	@After 
	public void teardown() {
		server1.stop();
		server1.clean();
		clients.forEach(x -> x.stop());
	}
	
	
	private ClientMailApplication addClient(ServerMailApplication server, String clientName) {
		ClientMailApplication $ = new ClientMailApplication(server.getAddress(), clientName);
		clients.add($);
		return $;
	}
	

	private void doSomeChat() throws Exception
	{
		server1.stop();
		server1.start();
		
		clientGal.sendMail("Ofer", "Bad job");
		clientOfer.sendMail("Gal", ":(");
		
		server1.stop();
		server1.start();
		
		clientGal.sendMail("Ofer", "jk");
		clientOfer.sendMail("Gal", ":)");
		
		clientIdan.sendMail("Ofer", "sup");
		clientOfer.sendMail("Idan", "good");
		clientIdan.sendMail("Ofer", "bye");
		
		
		server1.stop();
		server1.start();
		
		
		clientOfer.sendMail("Idan", "cya");
		
		clientTom.sendMail("Gal", "hi");
		clientGal.sendMail("Tom", "hey");
		server1.stop();
		server1.start();
		
		clientIdan.sendMail("Tom", "whoRU");
		
		server1.stop();
		server1.start();
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
	public void testGetCorrespondences() throws Exception
	{
		
		AssertCorrespondences(clientOfer, clientIdan, 1, new Mail[0]); 
		
		doSomeChat();
		
		
		AssertCorrespondences(clientOfer, clientIdan, 1, new Mail("Ofer", "Idan", "cya"));
		
		AssertCorrespondences(clientOfer, clientIdan, 2, 
				new Mail("Ofer", "Idan", "cya"), new Mail("Idan", "Ofer", "bye"));
		
		AssertCorrespondences(clientOfer, clientIdan, 3, 
				new Mail("Ofer", "Idan", "cya"), new Mail("Idan", "Ofer", "bye"),
				new Mail("Ofer", "Idan", "good"));
		
		
		AssertCorrespondences(clientOfer, clientIdan, 4, 
				new Mail("Ofer", "Idan", "cya"), new Mail("Idan", "Ofer", "bye"),
				new Mail("Ofer", "Idan", "good"), new Mail("Idan", "Ofer", "sup"));
		
		AssertCorrespondences(clientOfer, clientIdan, 5, 
				new Mail("Ofer", "Idan", "cya"), new Mail("Idan", "Ofer", "bye"),
				new Mail("Ofer", "Idan", "good"), new Mail("Idan", "Ofer", "sup"));
		
		
		AssertCorrespondences(clientTom, clientIdan, 5, 
				new Mail("Idan", "Tom", "whoRU"));
	}
	
	
	private void AssertCorrespondences(ClientMailApplication a, ClientMailApplication b,
			int howMany, Mail ... expected)
	{
		List<Mail> expectedList = Arrays.asList(expected);
		
		List<Mail> a_to_b = a.getCorrespondences(b.getUsername(), howMany);
		List<Mail> b_to_a = b.getCorrespondences(a.getUsername(), howMany);
		
		assertEquals(a_to_b, expectedList);
		assertEquals(b_to_a, expectedList);
	}
	
	
	@Test
	public void testGetSentMails() throws Exception
	{
		
		List<Mail> res;
		
		res = clientOfer.getSentMails(5);
		assertEquals(res, Arrays.asList(new Mail[0]));
		
		doSomeChat();
		
		res = clientGal.getSentMails(5);
		assertEquals(res, Arrays.asList(
			new Mail("Gal", "Tom", "hey"),
			new Mail("Gal", "Ofer", "jk"),
			new Mail("Gal", "Ofer", "Bad job")
			));

		server1.stop();
		server1.start();
		
		res = clientGal.getSentMails(2);
		assertEquals(res, Arrays.asList(
			new Mail("Gal", "Tom", "hey"),
			new Mail("Gal", "Ofer", "jk")
			));
	}

	
	@Test
	public void testgetIncomingMail() throws Exception
	{
		List<Mail> res;
		
		res = clientOfer.getIncomingMail(5);
		assertEquals(res, Arrays.asList(new Mail[0]));
		
		doSomeChat();
		
		
		res = clientGal.getIncomingMail(5);

		assertEquals(res, Arrays.asList(
			new Mail("Tom", "Gal", "hi"),
			new Mail("Ofer", "Gal", ":)"),
			new Mail("Ofer", "Gal", ":(")
			));
		
		server1.stop();
		server1.start();
		
		res = clientGal.getIncomingMail(2);
		assertEquals(res, Arrays.asList(
			new Mail("Tom", "Gal", "hi"),
			new Mail("Ofer", "Gal", ":)")
			));
		
	}
	
	
	@Test
	public void testGetAllMail() throws Exception
	{
		
		List<Mail> res;
		
		res = clientOfer.getAllMail(5);
		assertEquals(res, Arrays.asList(new Mail[0]));
		
		doSomeChat();
		
		res = clientGal.getAllMail(6);

		assertEquals(res, Arrays.asList(
				new Mail("Gal", "Tom", "hey"),
				new Mail("Tom", "Gal", "hi"),
				new Mail("Ofer", "Gal", ":)"),
				new Mail("Gal", "Ofer", "jk"),
				new Mail("Ofer", "Gal", ":("),
				new Mail("Gal", "Ofer", "Bad job")
			));
		
		server1.stop();
		server1.start();
		
		res = clientGal.getAllMail(3);

		assertEquals(res, Arrays.asList(
				new Mail("Gal", "Tom", "hey"),
				new Mail("Tom", "Gal", "hi"),
				new Mail("Ofer", "Gal", ":)")
			));
	}
	
	
	@Test
	public void testGetNewMail1() throws Exception
	{
		
		List<Mail> res;
		res = clientOfer.getNewMail();
		assertEquals(res, Arrays.asList(new Mail[0]));
		
		doSomeChat();
		
		
		server1.stop();
		server1.start();
		
		res = clientGal.getNewMail();
		assertEquals(res, Arrays.asList(
			new Mail("Tom", "Gal", "hi"),
			new Mail("Ofer", "Gal", ":)"),
			new Mail("Ofer", "Gal", ":(")
			));
				
		res = clientGal.getNewMail();
		assertEquals(res, Arrays.asList(new Mail[0]));
		
	}
	
	
	
	@Test
	public void shouldHaveNoContact()
	{
		List<String> contacts = clientIdan.getContacts(0);
		assertEquals(contacts, Arrays.asList(new String[0])); 
	}
	@Test
	public void shouldHaveHimselfAsContact() throws MessengerException
	{
		clientIdan.sendMail("Idan", "whoRU");
		List<String> contacts = clientIdan.getContacts(2);
		assertEquals(contacts, Arrays.asList("Idan"));
	}
	@Test
	public void contactsShouldBeSorted() throws Exception
	{
		doSomeChat();
		List<String> contacts = clientIdan.getContacts(2);
		assertEquals(contacts, Arrays.asList("Ofer","Tom"));
		
		server1.stop();
		server1.start();
		
		clientTom.sendMail("Ofer", "hi");
		
		
		contacts = clientTom.getContacts(3);
		assertEquals(contacts, Arrays.asList("Gal", "Idan", "Ofer"));
		
		contacts = clientTom.getContacts(2);
		assertEquals(contacts, Arrays.asList("Gal", "Idan", "Ofer"));
		
		
	}
}
