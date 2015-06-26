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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TChatIntegratedTest {

	final int CLIENTS_NUM = 20;
	
	private List<ClientChatApplication> clients;
	
	private ServerChatApplication server;
	
	private List<BlockingQueue<ChatMessage>> chatMessageQueus;
	private List<BlockingQueue<RoomAnnouncement>> announcementsQueus;
	
	@Rule
    public ExpectedException thrown = ExpectedException.none();
	
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
	
	
	@Test
	public void joinRoomAlreadyInWithServerReset() throws AlreadyInRoomException, InterruptedException {
		
		loginClient(0);
		
		clients.get(0).joinRoom("room1");

		//TODO: uncomment
//		logoutClient(0);
//		restartServer();
//		loginClient(0);
		
		
		try {
			clients.get(0).joinRoom("room1");
			//TODO: BUG. this causes a JOIN announcement to be sent to client 0.
		} catch (AlreadyInRoomException e)
		{
			logoutClient(0);

			assertAllBlockingQueuesAreCurrentlyEmpty();
			return;
		}
		fail();
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
	public void leaveRoomOccupiedByOneOther() throws NotInRoomException, AlreadyInRoomException, InterruptedException {
		loginClient(0);
		loginClient(1);
		
		clients.get(0).joinRoom("room1");
		clients.get(1).joinRoom("room1");
		
		clients.get(0).leaveRoom("room1");
		
		takeSingleAnnouncementAndAssertValue(1,
				new RoomAnnouncement(clients.get(0).getUsername(),"room1", Announcement.LEAVE));
		
		logoutClient(0);
		logoutClient(1);
		
	}
	
	
	
	@Test
	public void logsoutWhenInRoomOccupiedByOneOther() throws NotInRoomException, AlreadyInRoomException, InterruptedException {
		loginClient(0);
		loginClient(1);
		
		clients.get(0).joinRoom("room1");
		clients.get(1).joinRoom("room1");
		
		logoutClient(0);
		
		takeSingleAnnouncementAndAssertValue(1,
				new RoomAnnouncement(clients.get(0).getUsername(),"room1", Announcement.DISCONNECT));
			
		logoutClient(1);
		
	}
	
	
	
	@Test (timeout = 3000)
	public void logoutAndThenLogInAgain() throws NotInRoomException, AlreadyInRoomException, InterruptedException {
		loginClient(0);
		loginClient(1);
		
		clients.get(0).joinRoom("room1");
		clients.get(1).joinRoom("room1");
		
		takeSingleAnnouncementAndAssertValue(0,
		new RoomAnnouncement(clients.get(1).getUsername(),"room1", Announcement.JOIN));

		
		Thread.sleep(100);
		
		logoutClient(0);
		
		takeSingleAnnouncementAndAssertValue(1,
				new RoomAnnouncement(clients.get(0).getUsername(),"room1", Announcement.DISCONNECT));
		
		loginClient(0);
		
		takeSingleAnnouncementAndAssertValue(1,
				new RoomAnnouncement(clients.get(0).getUsername(),"room1", Announcement.JOIN));
		
	
		logoutClient(1);
		
		takeSingleAnnouncementAndAssertValue(0,
				new RoomAnnouncement(clients.get(1).getUsername(),"room1", Announcement.DISCONNECT));
		
		logoutClient(0);
		
		assertAllBlockingQueuesAreCurrentlyEmpty();
				
	}
	
	

	@Test //(timeout = 1000)//TODO  
	public void logsOutSendingAnnouncmentsToOthers() throws InterruptedException, NotInRoomException, AlreadyInRoomException {
		testAnnouncementsWhenUserLeaves(true);
	}
	
	@Test //(timeout = 1000)//TODO  
	public void leaveRoomsSendingAnnouncmentsToOthers() throws InterruptedException, NotInRoomException, AlreadyInRoomException {
		testAnnouncementsWhenUserLeaves(false);
	}	


	@Test
	public void sendMessageToNobody() throws AlreadyInRoomException, NotInRoomException
	{
		loginClient(0);
		loginClient(1);
		
		clients.get(0).joinRoom("room1");
		clients.get(0).sendMessage("room1", "hi");
		logoutClient(0);
		
		assertAllBlockingQueuesAreCurrentlyEmpty();
		logoutClient(1);
	}
	
	@Test
	public void sendMessageWhenNotInRoom() throws AlreadyInRoomException, NotInRoomException
	{
		loginClient(0);

		clients.get(0).joinRoom("room1");

		try
		{
			clients.get(0).sendMessage("room2", "hi");
			fail();
		} catch (NotInRoomException e)
		{
		}
		catch (Exception e)
		{
			fail();
		}
		
		logoutClient(0);
	}
	
	
	@Test
	public void sendMessageToSingleRecepient() throws AlreadyInRoomException, NotInRoomException, InterruptedException
	{
		loginClient(0);
		loginClient(1);

		clients.get(0).joinRoom("room1");
		clients.get(1).joinRoom("room1");

		
		takeSingleAnnouncementAndAssertValue(0, 
				new RoomAnnouncement(clients.get(1).getUsername(),"room1", Announcement.JOIN));

		
		clients.get(0).sendMessage("room1", "hi");
		
		takeSingleMessegeAndAssertValue(1, new ChatMessage(
				clients.get(0).getUsername(), "room1", "hi"));
		
		
		logoutClient(0);
		
		takeSingleAnnouncementAndAssertValue(1, 
				new RoomAnnouncement(clients.get(0).getUsername(),"room1", Announcement.DISCONNECT));

		logoutClient(1);
		
	
		assertAllBlockingQueuesAreCurrentlyEmpty();
	}
	
	@Test
	public void sendMessageToMultiplePeople() throws AlreadyInRoomException, InterruptedException, NotInRoomException
	{
		loginClient(0);
		loginClient(1);
		loginClient(2);

		clients.get(0).joinRoom("room1");
		clients.get(1).joinRoom("room1");
		clients.get(2).joinRoom("room1");

		clients.get(0).sendMessage("room1", "hi");
		
		takeSingleMessegeAndAssertValue(1, new ChatMessage(
				clients.get(0).getUsername(), "room1", "hi"));

		takeSingleMessegeAndAssertValue(2, new ChatMessage(
				clients.get(0).getUsername(), "room1", "hi"));

		
		logoutClient(0);
		logoutClient(1);
		logoutClient(2);
		
	}
	
	/**
	 * 
	 * @param isLogout - if true, then we check the scenario where a users leave by logs-out, otherwise we check
	 * the scenario where a users simply leaves the room without logout.
	 * @throws InterruptedException 
	 * @throws NotInRoomException 
	 * @throws AlreadyInRoomException 
	 */
	private void testAnnouncementsWhenUserLeaves(boolean isLogout) throws InterruptedException, NotInRoomException, AlreadyInRoomException
	{
		Announcement leaveType = isLogout ? Announcement.DISCONNECT : Announcement.LEAVE;
			 
				
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
		
		
		takeSingleAnnouncementAndAssertValue(0, 
				new RoomAnnouncement(clients.get(1).getUsername(),"room1", Announcement.JOIN));
		
		clients.get(2).joinRoom("room3");
		clients.get(2).joinRoom("room4");
		

		clients.get(3).joinRoom("room5");

		takeSingleAnnouncementAndAssertValue(0, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room5", Announcement.JOIN));

		
		clients.get(3).joinRoom("room1");

		takeSingleAnnouncementAndAssertValue(0, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room1", Announcement.JOIN));

		takeSingleAnnouncementAndAssertValue(1, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room1", Announcement.JOIN));
		
		
		assertAllBlockingQueuesAreCurrentlyEmpty();
		
		/////////////// Step 2 : user 3 leaves and then comes back again.  ///////////////
		
		if (isLogout)
			logoutClient(3);
		else {
			clients.get(3).leaveRoom("room1");
			clients.get(3).leaveRoom("room5");
		}
			
		
		takeExactlyTwoAnnouncementAndAssertValues(0, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room1", leaveType),
				new RoomAnnouncement(clients.get(3).getUsername(),"room5", leaveType));

		takeSingleAnnouncementAndAssertValue(1, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room1", leaveType));
		
		if (isLogout)
			loginClient(3);
		else {
			clients.get(3).joinRoom("room1");
			clients.get(3).joinRoom("room5");
		}
		
		
		takeExactlyTwoAnnouncementAndAssertValues(0, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room1", Announcement.JOIN),
				new RoomAnnouncement(clients.get(3).getUsername(),"room5", Announcement.JOIN));

		takeSingleAnnouncementAndAssertValue(1, 
				new RoomAnnouncement(clients.get(3).getUsername(),"room1", Announcement.JOIN));
		

		
		assertAllBlockingQueuesAreCurrentlyEmpty();
		
	}

	/* Asserts a client receives exactly a single message, and no others. */
	private void takeSingleMessegeAndAssertValue(int clientInd, ChatMessage expectedMsg)
			throws InterruptedException {
		ChatMessage msg = chatMessageQueus.get(clientInd).take();
		assertEquals(msg, expectedMsg);
		assertEquals(null, chatMessageQueus.get(clientInd).poll(100, TimeUnit.MILLISECONDS) );
	}
	
	/* Asserts a client receives exactly one given announcement, and no others. */
	private void takeSingleAnnouncementAndAssertValue(int clientInd, RoomAnnouncement expectedRoomAnnouncement)
			throws InterruptedException {
		RoomAnnouncement ra = announcementsQueus.get(clientInd).take();
		assertEquals(ra, expectedRoomAnnouncement);
		assertEquals(null, announcementsQueus.get(clientInd).poll(100, TimeUnit.MILLISECONDS) );
	}
	
	
	/* Asserts a client receives exactly two announcement (order doesn't matter), and no others. */
	private void takeExactlyTwoAnnouncementAndAssertValues(
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
	
	
	
	/**
	 * Waits a bit and makes sure all queues are empty.
	 */
	private void assertAllBlockingQueuesAreCurrentlyEmpty()
	{
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			throw new RuntimeException();
		}
		assertFalse(announcementsQueus.stream().anyMatch(x -> x.peek() != null));
		assertFalse(chatMessageQueus.stream().anyMatch(x -> x.peek() != null));
	}
	
}