package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.lib.ClientLib;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/*************************************************************
 * *************************************************************
 * *************************************************************
 * Note:
 * This unit test uses Mockito to test basic behavior of 
 * ClientChatApplicationTest, independently from the server.
 * The "TChatIntegratedTest" under app-chat-tests tests the functionality
 * of the app extensively.
 * *************************************************************
 * *************************************************************
 *************************************************************/

public class ClientChatApplicationTest {

	private ClientChatApplication chatClient;
	ClientLib clientMock;
	
	@Before
	public void setUp() throws Exception {
		chatClient = new ClientChatApplication("server1", "user1");
		clientMock = Mockito.mock(ClientLib.class);
		
		Mockito.when(clientMock.sendRecieve(Mockito.any(), Mockito.any())).thenReturn(null);
		
		chatClient.setClient(clientMock);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
