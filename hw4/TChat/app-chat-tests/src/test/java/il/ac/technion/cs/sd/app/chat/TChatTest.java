package il.ac.technion.cs.sd.app.chat;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.app.chat.RoomAnnouncement.Announcement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TChatTest {
	private ServerChatApplication server = new ServerChatApplication("Server");
	private Collection<ClientChatApplication> clients = new LinkedList<>();
	// all listened to incoming messages will be written here
	private Map<String, BlockingQueue<RoomAnnouncement>> announcements;
	private Map<String, BlockingQueue<ChatMessage>> messages;

	@Before
	public void setup() {
		server.start(); // non-blocking
	}

	@After
	public void teardown() {
		clients.forEach(ClientChatApplication::logout);
		clients.forEach(ClientChatApplication::stop);
		server.stop();
		server.clean();
	}

	@Test
	public void messagesTest() throws Exception {
		ClientChatApplication gal = loginUser("Gal");
		ClientChatApplication itay = loginUser("Itay");
		gal.joinRoom("room");
		assertEquals(announcements.get("Gal").take(), new RoomAnnouncement("Gal", "room", Announcement.JOIN));
		itay.joinRoom("room");
		assertEquals(announcements.get("Gal").take(), new RoomAnnouncement("Itay", "room", Announcement.JOIN));
		itay.sendMessage("room", "Hi all");
		assertEquals(messages.get("Gal").take(), new ChatMessage("Itay", "room", "Hi all"));
	}

	@Test
	public void roomTest() throws Exception {
		ClientChatApplication gal = loginUser("Gal");
		ClientChatApplication itay = loginUser("Itay");
		itay.joinRoom("room");
		assertEquals(Arrays.asList("room"), itay.getJoinedRooms());
		assertEquals(Arrays.asList("Itay"), gal.getClientsInRoom("room"));
		itay.logout();
		gal.joinRoom("room2");
		assertEquals(Arrays.asList("room"), gal.getClientsInRoom("room2"));
		assertEquals(Arrays.asList("room2"), gal.getAllRooms());
		gal.leaveRoom("room");
		assertEquals(Collections.EMPTY_LIST, gal.getAllRooms());
		try {
			gal.getClientsInRoom("room");
			fail("Should have thrown an exception, since room is empty");
		} catch(NoSuchRoomException e) {}
		gal.logout();
		
	}

	private ClientChatApplication loginUser(String name) {
		ClientChatApplication $ = new ClientChatApplication(server.getAddress(), name);
		clients.add($);
		announcements.put(name, new LinkedBlockingQueue<>());
		messages.put(name, new LinkedBlockingQueue<>());
		$.login(x -> messages.get(name).add(x), x -> announcements.get(name).add(x));
		clients.add($);
		return $;
	}
}
