package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import il.ac.technion.cs.sd.lib.ServerLib;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;




/*************************************************************
 * *************************************************************
 * *************************************************************
 * Note:
 * This unit test uses Mockito to test basic behavior of 
 * ServerChatApplication, independently from the client.
 * The "TChatIntegratedTest" under app-chat-tests tests the logic of
 * the app extensively.
 * *************************************************************
 * *************************************************************
 *************************************************************/


public class ServerChatApplicationTest {

	ServerChatApplication chatServer;
	ServerLib mockServer;

	
	@Before
	public void setUp() throws Exception {
		chatServer = new ServerChatApplication("server_" + UUID.randomUUID());
		mockServer = Mockito.mock(ServerLib.class);
		chatServer.setServer(mockServer);
		Mockito.when(mockServer.readObjectFromFile(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
		Mockito.when(mockServer.readObjectFromFile(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
		Mockito.when(mockServer.readObjectFromFile(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
		Mockito.when(mockServer.readObjectFromFile(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
	}

	@After
	public void tearDown() throws Exception {
	
	}

	@Test
	public void startListenLoop() {
		chatServer.start();
		Mockito.verify(mockServer,Mockito.times(1)).start(Mockito.any(), Mockito.any());
		Mockito.verify(mockServer,Mockito.atLeastOnce()).readObjectFromFile(Mockito.any(), Mockito.any());
	}
	
	@Test
	public void saveFilesOnStop() {
		chatServer.stop();
		Mockito.verify(mockServer,Mockito.atLeastOnce()).saveObjectToFile(Mockito.anyString(), Mockito.any());
	}
	
	@Test
	public void cleanFiles() {
		chatServer.clean();
		Mockito.verify(mockServer,Mockito.times(1)).clearPersistentData();
	}
}
