package il.ac.technion.cs.sd.app.chat;

import il.ac.technion.cs.sd.lib.ClientLib;

import java.util.UUID;

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

	private String clientAddress;
	private String serverAddress;
	
	private ClientChatApplication chatClient;
	ClientLib clientMock;
	
	@Before
	public void setUp() throws Exception {
		clientAddress = "client_" + UUID.randomUUID();
		serverAddress = "server_" + UUID.randomUUID();
		
		chatClient = new ClientChatApplication(serverAddress, clientAddress);
		clientMock = Mockito.mock(ClientLib.class);
		
		Mockito.when(clientMock.sendRecieve(Mockito.any(), Mockito.any())).thenReturn(null);
		
		chatClient.setClient(clientMock);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void login() {
		chatClient.login(x->{},y->{});
		
		Mockito.verify(clientMock,Mockito.times(1)).start(
				Mockito.eq(serverAddress),Mockito.any(),Mockito.any());
		
		Mockito.verify(clientMock,Mockito.times(1)).blockingSend(Mockito.any());
			
	}
	
	
	@Test
	public void joinRoom() throws AlreadyInRoomException {
		Utils.assertThrow(()-> chatClient.joinRoom("room1"), NullPointerException.class);
		Mockito.verify(clientMock,Mockito.times(1)).sendRecieve(Mockito.any(),Mockito.any());
	}
	
	@Test
	public void leaveRoom() throws AlreadyInRoomException, NotInRoomException {
		Utils.assertThrow(()-> chatClient.leaveRoom("room1"), NullPointerException.class);
		Mockito.verify(clientMock,Mockito.times(1)).sendRecieve(Mockito.any(),Mockito.any());
	}
	
	@Test
	public void sendMessage() throws AlreadyInRoomException, NotInRoomException {
		Utils.assertThrow(()-> chatClient.sendMessage("room1", "hi"), NullPointerException.class);
		Mockito.verify(clientMock,Mockito.times(1)).sendRecieve(Mockito.any(),Mockito.any());
	}

	@Test
	public void getJoinedRooms() throws AlreadyInRoomException, NotInRoomException {
		Utils.assertThrow(()-> chatClient.getJoinedRooms(), NullPointerException.class);
		Mockito.verify(clientMock,Mockito.times(1)).sendRecieve(Mockito.any(),Mockito.any());
	}

	@Test
	public void getAllRooms() throws AlreadyInRoomException, NotInRoomException {
		Utils.assertThrow(()-> chatClient.getAllRooms(), NullPointerException.class);
		Mockito.verify(clientMock,Mockito.times(1)).sendRecieve(Mockito.any(),Mockito.any());
	}
	
	@Test
	public void getClientsInRoom() throws AlreadyInRoomException, NotInRoomException {
		Utils.assertThrow(()-> chatClient.getClientsInRoom("room1"), NullPointerException.class);
		Mockito.verify(clientMock,Mockito.times(1)).sendRecieve(Mockito.any(),Mockito.any());
	}
	
}
