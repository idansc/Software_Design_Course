package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import il.ac.technion.cs.sd.app.chat.RoomAnnouncement.Announcement;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
		
		assertAllBlockingQueuesAreCurrentlyEmpty();
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
		
		assertAllBlockingQueuesAreCurrentlyEmpty();
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
		
		assertAllBlockingQueuesAreCurrentlyEmpty();
	}
	
	@Test
	public void leaveRoomOccupiedByOthers() throws NotInRoomException, AlreadyInRoomException, InterruptedException {
		loginClient(0);
		loginClient(1);
		
		clients.get(0).joinRoom("room1");
		clients.get(1).joinRoom("room1");
		
		clients.get(0).leaveRoom("room1");
		
		assertClientGetsSingleAnnouncement(1,
				new RoomAnnouncement(clients.get(0).getUsername(),"room1", Announcement.LEAVE));
		
		logoutClient(0);
		logoutClient(1);
		
	}
	
	
	
	@Test
	public void logsoutWhenInRoomOccupiedByOthers() throws NotInRoomException, AlreadyInRoomException, InterruptedException {
		loginClient(0);
		loginClient(1);
		
		clients.get(0).joinRoom("room1");
		clients.get(1).joinRoom("room1");
		
		logoutClient(0);
		
		assertClientGetsSingleAnnouncement(1,
				new RoomAnnouncement(clients.get(0).getUsername(),"room1", Announcement.DISCONNECT));
			
		logoutClient(1);
		
	}
	
	
	
	@Test (timeout = 1000)
	public void logoutAndThenLogInAgain() throws NotInRoomException, AlreadyInRoomException, InterruptedException {
		loginClient(0);
		loginClient(1);
		
		clients.get(0).joinRoom("room1");
		clients.get(1).joinRoom("room1");
		
		logoutClient(0);
		
		assertClientGetsSingleAnnouncement(1,
				new RoomAnnouncement(clients.get(0).getUsername(),"room1", Announcement.DISCONNECT));
		
		loginClient(0);
		
		assertClientGetsSingleAnnouncement(1,
				new RoomAnnouncement(clients.get(0).getUsername(),"room1", Announcement.JOIN));
		
		//TODO: Seems like a bug, we are waiting forever here.
		
		
		
		
//TODO		
//		logoutClient(1);
//		
//		assertClientGetsSingleAnnouncement(0,
//				new RoomAnnouncement(clients.get(1).getUsername(),"room1", Announcement.DISCONNECT));
//		
//		logoutClient(0);
//		
//		assertAllBlockingQueuesAreCurrentlyEmpty();
				
	}
	
	

//TODO
//	@Test (timeout = 1000)//TODO: set good time 
//	public void logsOutSendingAnnouncmentsToOthers() throws InterruptedException, NotInRoomException, AlreadyInRoomException {
//		testAnnouncementsWhenUserLeaves(true);
//	}
	
		


	/**
	 * 
	 * @param isLogout - if true, then we check the scenario where a user logs-out, otherwise we check
	 * the scenario where a user simply leaves the room.
	 * @throws InterruptedException 
	 * @throws NotInRoomException 
	 * @throws AlreadyInRoomException 
	 */
	private void testAnnouncementsWhenUserLeaves(boolean isLogout) throws InterruptedException, NotInRoomException, AlreadyInRoomException
	{
		// The user we focus on is user 0.
		
		loginClient(0); // in rooms: 1, 5, 9
		loginClient(1); // in rooms: 1, 2
		loginClient(2); // in rooms: 3, 4
		loginClient(3); // in rooms: 1, 5
		
		
		/////////////// Step 1 : everybody join rooms ///////////////
		clients.get(0).joinRoom("room1");
		clients.get(0).joinRoom("room5");
		clients.get(0).joinRoom("room9");

		clients.get(1).joinRoom("room1");
		clients.get(1).joinRoom("room2");
		
		
		assertClientGetsSingleAnnouncement(0, 
				new RoomAnnouncement(clients.get(1).getUsername(),"room1", Announcement.JOIN));
		
		clients.get(2).joinRoom("room3");
		clients.get(2).joinRoom("room4");
		

		clients.get(3).joinRoom("room5");

		assertClientGetsSingleAnnouncement(0, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room5", Announcement.JOIN));

		
		clients.get(3).joinRoom("room1");

		assertClientGetsSingleAnnouncement(0, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room1", Announcement.JOIN));

		assertClientGetsSingleAnnouncement(1, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room1", Announcement.JOIN));
		
		
		assertAllBlockingQueuesAreCurrentlyEmpty();
		
		/////////////// Step 2 : user 3 logs out and then logs in again  ///////////////
		
		logoutClient(3);
		
		assertClientGetsExactlyTwoAnnouncement(0, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room1", Announcement.DISCONNECT),
				new RoomAnnouncement(clients.get(3).getUsername(),"room5", Announcement.DISCONNECT));

		assertClientGetsSingleAnnouncement(1, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room1", Announcement.DISCONNECT));
		
		loginClient(3);
		
		
		assertClientGetsSingleAnnouncement(0, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room1", Announcement.JOIN));

		assertClientGetsSingleAnnouncement(1, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room1", Announcement.JOIN));
		
		
		/////////////// Step 3 :
		//TODO
		
//		
//		if (isLogout)
//			logoutClient(0);
//		else
//			clients.get(0).leaveRoom("room1");
//		
//		
//		RoomAnnouncement expectedRoomAnnouncement = 
//				new RoomAnnouncement(clients.get(0).getUsername(),"room1", 
//						isLogout ? Announcement.DISCONNECT : Announcement.LEAVE );
//		
//		
//		assertClientGetsSingleAnnouncement(1, expectedRoomAnnouncement);		
//		assertClientGetsSingleAnnouncement(3, expectedRoomAnnouncement);
//		
//		
//		assertClientDoesNotGetAnAnnouncement(0);
//		assertClientDoesNotGetAnAnnouncement(2);
//		
//		
//		logoutClient(1);
//		logoutClient(2);
//		logoutClient(3);
//		
//		if (!isLogout) // otherwise we've already logged out.
//			logoutClient(0); 
//		
//		assertAllBlockingQueuesAreCurrentlyEmpty();
		
	}

	private void assertClientGetsSingleAnnouncement(int clientInd, RoomAnnouncement expectedRoomAnnouncement)
			throws InterruptedException {
		RoomAnnouncement ra = announcementsQueus.get(clientInd).take();
		assertEquals(ra, expectedRoomAnnouncement);
		assertEquals(null, announcementsQueus.get(clientInd).poll(100, TimeUnit.MILLISECONDS) );
	}
	
	
	/* Assers a client receives exactly two announcement (order doesn't matter */
	private void assertClientGetsExactlyTwoAnnouncement(
			int clientInd, RoomAnnouncement announcement1, RoomAnnouncement announcement2)
					throws InterruptedException {
		
		Set<RoomAnnouncement> announcements = new HashSet<>();
		announcements.add(announcement1);
		announcements.add(announcement2);
		
		for (int i=0; i<2; i++)
		{
			RoomAnnouncement ra = announcementsQueus.get(clientInd).take();
			assertTrue(announcements.remove(ra));
		}
		
		assertEquals(null, announcementsQueus.get(clientInd).poll(100, TimeUnit.MILLISECONDS) );
	}
	
	
	private void assertClientDoesNotGetAnAnnouncement(int clientInd) throws InterruptedException
	{
		assertEquals(null, announcementsQueus.get(clientInd).poll(100, TimeUnit.MILLISECONDS) );
	}
	
	
	private void assertAllBlockingQueuesAreCurrentlyEmpty()
	{
		assertFalse(announcementsQueus.stream().anyMatch(x -> x.peek() != null));
		assertFalse(chatMessageQueus.stream().anyMatch(x -> x.peek() != null));
	}
	
}