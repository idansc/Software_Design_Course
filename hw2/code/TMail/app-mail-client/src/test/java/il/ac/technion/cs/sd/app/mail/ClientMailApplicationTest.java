package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.lib.clientserver.Client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ClientMailApplicationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void sendMail() {
		Client clientMock = Mockito.mock(Client.class);
		Mockito.when(clientMock.sendToServerAndGetAnswer("server1", any())
		ClientMailApplication clientMail = 
				new ClientMailApplication("server1","user1");
		
		
		
	}

}
