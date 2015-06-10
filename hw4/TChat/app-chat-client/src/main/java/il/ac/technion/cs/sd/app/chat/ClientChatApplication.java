package il.ac.technion.cs.sd.app.chat;

import java.util.List;
import java.util.function.Consumer;

/**
 * The client side of the TChat application. Allows sending and getting messages
 * to and from other clients using a server. <br>
 * You should implement all the methods in this class
 */
public class ClientChatApplication {

	/**
	 * Creates a new application, tied to a single user
	 * 
	 * @param serverAddress The address of the server to connect to for sending and
	 *            receiving messages
	 * @param username The username that will be sending and accepting the messages
	 *            using this object
	 */
	public ClientChatApplication(String serverAddress, String username) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Logs the user to the server. The user automatically joins all the rooms he was
	 * joined to to before logging out. 
	 * The server can only reply to this message using an empty message.
	 * Client receive their own messages and announcements, e.g., a client also receives his own messages. 
	 * @param chatMessageConsumer The consumer of chat messages 
	 * 		(See {@link ClientChatApplication#sendMessage(String, String)}) 
	 * @param announcementConsumer The consumer of room announcements
	 * 		(See {@link RoomAnnouncement.Announcement})
	 */
	public void login(Consumer<ChatMessage> chatMessageConsumer,
			Consumer<RoomAnnouncement> announcementConsumer) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Joins the room. If the room does not exist, it will be created.
	 * @param room The room to join
	 * @throws AlreadyInRoomException If the client isn't currently in the room
	 */
	public void joinRoom(String room) throws AlreadyInRoomException {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Leaves the room. 
	 * @param room The room to leave
	 * @throws NotInRoomException If the client isn't currently in the room
	 */
	public void leaveRoom(String room) throws NotInRoomException {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Logs the user out of chat application. A logged out client perform any tasks other than logging in.
	 */
	public void logout() {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Broadcasts a message to all other clients in the room, including the room the client is in. 
	 * @param room The room to broadcast the message to.
	 * @param what The message to broadcast.
	 * @throws NotInRoomException If the client isn't currently in the room
	 */
	public void sendMessage(String room, String what) throws NotInRoomException {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * @return All the rooms the client joined
	 */
	public List<String> getJoinedRooms() {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * @return all rooms that have clients currently online, i.e., logged in them
	 */
	public List<String> getAllRooms() {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * Gets all the clients that joined the room and are currently logged in. A client
	 * does not have to be in a room to get a list of its clients
	 * @param room The room to check
	 * @return A list of all the online clients in the room
	 * @throws NoSuchRoomException If the room doesn't exist, or no clients are currently in it
	 */
	public List<String> getClientsInRoom(String room) throws NoSuchRoomException {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Stops the client, freeing up any resources used.
	 * You can assume that {@link ClientChatApplication#logout()} was called before this method if the client
	 * was logged in.
	 */
	public void stop() {
		throw new UnsupportedOperationException("Not implemented");
	}

}
