package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.fail;
import il.ac.technion.cs.sd.app.chat.RoomAnnouncement.Announcement;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TChatIntegratedTest {

	final int CLIENTS_NUM = 20;
	
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


		
		server = new ServerChatApplication("server_" + UUID.randomUUID());
		server.clean();
		server.start();
		
		clients = new LinkedList<>(); 
		chatMessageQueus = new LinkedList<>();
		announcementsQueus = new LinkedList<>();			
		for (int i=0; i<CLIENTS_NUM; i++)
		{
			addClient(server.getAddress(), "client_" + UUID.randomUUID());
			chatMessageQueus.add(new LinkedBlockingQueue<>());
			announcementsQueus.add(new LinkedBlockingQueue<>());
		}

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
	
	
	@Test
	public void leaveRoomNotCurrentlyInWithServerRestart() throws NotInRoomException, AlreadyInRoomException {
		
		loginClient(0);
		
		
		try{
			clients.get(0).leaveRoom("room1");
			fail();
		} catch (NotInRoomException e)
		{
		} catch (Exception e)
		{
			fail();
		}
		
		clients.get(0).joinRoom("room1");
		
		logoutClient(0);
		restartServer();
		loginClient(0);
		
		clients.get(0).leaveRoom("room1");
		
		try{
			clients.get(0).leaveRoom("room1");
			fail();
		} catch (NotInRoomException e)
		{
		} catch (Exception e)
		{
			fail();
		}
		
		logoutClient(0);
	}
	
	@Test
	public void leaveRoomOccupiedByOthers() throws NotInRoomException, AlreadyInRoomException, InterruptedException {
		loginClient(0);
		loginClient(1);
		
		clients.get(0).joinRoom("room1");
		clients.get(1).joinRoom("room1");
		
		clients.get(0).leaveRoom("room1");
		RoomAnnouncement ra = announcementsQueus.get(1).take();
		
		assertEquals(ra, new RoomAnnouncement(clients.get(0).getUsername(),"room1", Announcement.LEAVE));
		
		logoutClient(0);
		logoutClient(1);
	}
		
	
	


}
