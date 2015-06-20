package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TChatIntegratedTest {

	private List<ClientChatApplication> clients;
	
	private ServerChatApplication server;
	
	private List<BlockingQueue<ChatMessage>> chatMessageQueus;
	private List<BlockingQueue<RoomAnnouncement>> announcementsQueus;
	
	
	private void addClient(String serverAddress, String username)
	{
		ClientChatApplication $ = new ClientChatApplication(serverAddress,username);
		clients.add($);
	}
	
	private <T> Consumer<T> createConsumer(BlockingQueue<T> queue)
	{
		return x -> queue.add(x);
	}
	
	private void loginClient(int index) {
		clients.get(index).login( 
				createConsumer(chatMessageQueus.get(index)),
				createConsumer(announcementsQueus.get(index)) );
	}
	
	private void logoutClient(int index)
	{
		clients.get(index).logout();
	}
	
	private void restartServer()
	{
		String address = server.getAddress();
		server.stop();
		server = new ServerChatApplication(address);
		server.start();
	}
	
	@Before
	public void setUp() throws Exception {


		chatMessageQueus = new LinkedList<>();
		announcementsQueus = new LinkedList<>();			
		for (int i=0; i<20; i++)
		{
			chatMessageQueus.add(new LinkedBlockingQueue<>());
			announcementsQueus.add(new LinkedBlockingQueue<>());
		}
		
		server = new ServerChatApplication("server_" + UUID.randomUUID());
		server.clean();
		server.start();
		
		clients = new LinkedList<>(); 
		addClient(server.getAddress(), "client_" + UUID.randomUUID());
	}

	@After
	public void tearDown() throws Exception {
		clients.stream().forEach(c -> c.stop());
		
		server.stop();
		server.clean();
	}
	

	@Test (expected = AlreadyInRoomException.class)
	public void joinRoomAlreadyIn() throws AlreadyInRoomException {
		
		loginClient(0);
		
		try{
			clients.get(0).joinRoom("room1");
		} catch (AlreadyInRoomException e)
		{
			fail();
		}
		clients.get(0).joinRoom("room1");
		
		logoutClient(0);		
	}
	
	
	@Test (expected = AlreadyInRoomException.class)
	public void joinRoomAlreadyInWithServerReset() throws AlreadyInRoomException {
		
		loginClient(0);
		
		try{
			clients.get(0).joinRoom("room1");
		} catch (AlreadyInRoomException e)
		{
			fail();
		}
		
		logoutClient(0);
		restartServer();
		loginClient(0);
		
		clients.get(0).joinRoom("room1");
		
		logoutClient(0);
	}
	
	


}
