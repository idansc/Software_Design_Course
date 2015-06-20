package il.ac.technion.cs.sd.app.chat;

import il.ac.technion.cs.sd.lib.clientserver.Client;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.google.gson.reflect.TypeToken;

/**
 * The client side of the TChat application. Allows sending and getting messages
 * to and from other clients using a server. <br>
 * You should implement all the methods in this class
 */
public class ClientChatApplication {
	Client _client;
	String _serverAdress;
	Boolean _isOnline;
	

	/**
	 * Creates a new application, tied to a single user
	 * 
	 * @param serverAddress The address of the server to connect to for sending and
	 *            receiving messages
	 * @param username The username that will be sending and accepting the messages
	 *            using this object
	 */
	public ClientChatApplication(String serverAddress, String username) {
		_serverAdress = serverAddress;
		_client = new Client(username);
		_isOnline = false;
	}
	
	public String getUsername()
	{
		return _client.getAddress();
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
		_client.start(_serverAdress, new ClientMessageConsumer(chatMessageConsumer,announcementConsumer), ClientMessage.class);
		
		_client.send(new ServerMessage(TaskServerType.LOGIN));
		_isOnline = true;
	}

	/**
	 * Joins the room. If the room does not exist, it will be created.
	 * @param room The room to join
	 * @throws AlreadyInRoomException If the client isn't currently in the room
	 */
	public void joinRoom(String room) throws AlreadyInRoomException {
		Optional<Boolean> $ = _client.sendAndBlockUntilResponseArrives(new ServerMessage(TaskServerType.JOIN_ROOM).setRoom(room), 
				new TypeToken<Optional<Boolean>>(){}.getType());
		$.orElseThrow(AlreadyInRoomException::new);
	}

	/**
	 * Leaves the room. 
	 * @param room The room to leave
	 * @throws NotInRoomException If the client isn't currently in the room
	 */
	public void leaveRoom(String room) throws NotInRoomException {
		Optional<Boolean> $ = _client.sendAndBlockUntilResponseArrives(new ServerMessage(TaskServerType.LEAVE_ROOM).setRoom(room), 
				new TypeToken<Optional<Boolean>>(){}.getType());
		$.orElseThrow(NotInRoomException::new);
	}

	/**
	 * Logs the user out of chat application. A logged out client perform any tasks other than logging in.
	 */
	public void logout() {
		if(_isOnline){
			_client.send(new ServerMessage(TaskServerType.LOGOUT));
			_client.stopListenLoop();
			_isOnline = false;
		}
	}

	/**
	 * Broadcasts a message to all other clients in the room, including the room the client is in. 
	 * @param room The room to broadcast the message to.
	 * @param what The message to broadcast.
	 * @throws NotInRoomException If the client isn't currently in the room
	 */
	public void sendMessage(String room, String what) throws NotInRoomException {
		Optional<Boolean> $ = _client.<ServerMessage,Optional<Boolean>>sendAndBlockUntilResponseArrives(new ServerMessage(TaskServerType.SEND_MESSAGE)
			.setChatMessage(new ChatMessage(_client.getAddress(), room, what)), 
				new TypeToken<Optional<Boolean>>(){}.getType());
		$.orElseThrow(NotInRoomException::new);
	}

	/**
	 * @return All the rooms the client joined
	 */
	public List<String> getJoinedRooms() {
		Optional<List<String>> $ = _client.sendAndBlockUntilResponseArrives(new ServerMessage(TaskServerType.GET_JOINED_ROOM), 
			new TypeToken<Optional<List<String>>>(){}.getType());
		return $.orElse(null);
	}

	/**
	 * @return all rooms that have clients currently online, i.e., logged in them
	 */
	public List<String> getAllRooms() {
		Optional<List<String>> $ = _client.sendAndBlockUntilResponseArrives(new ServerMessage(TaskServerType.GET_ALL_ROOMS), 
				new TypeToken<Optional<List<String>>>(){}.getType());
			return $.orElse(null);
	}
	
	/**
	 * Gets all the clients that joined the room and are currently logged in. A client
	 * does not have to be in a room to get a list of its clients
	 * @param room The room to check
	 * @return A list of all the online clients in the room
	 * @throws NoSuchRoomException If the room doesn't exist, or no clients are currently in it
	 */
	public List<String> getClientsInRoom(String room) throws NoSuchRoomException {
		Optional<List<String>> $ = _client.sendAndBlockUntilResponseArrives(new ServerMessage(TaskServerType.GET_CLIENTS_IN_ROOM).setRoom(room), 
				new TypeToken<Optional<List<String>>>(){}.getType());
			return $.orElseThrow(NoSuchRoomException::new);
	}

	/**
	 * Stops the client, freeing up any resources used.
	 * You can assume that {@link ClientChatApplication#logout()} was called before this method if the client
	 * was logged in.
	 */
	public void stop() {
		//no need. 
	}

}
