package il.ac.technion.cs.sd.app.msg;

import il.ac.technion.cs.sd.lib.clientserver.Client;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;

/**
 * The client side of the TMail application. Allows sending and getting messages to and from other clients using a server. <br>
 * You should implement all the methods in this class
 */
public class ClientMsgApplication {
	
	private String _userName;
	private Client client;
	private String _serverAddress;

	
	/**
	 * Creates a new application, tied to a single user
	 * 
	 * @param serverAddress The address of the server to connect to for sending and receiving messages
	 * @param username The username that will be sending and accepting the messages using this object
	 */
	public ClientMsgApplication(String serverAddress, String username) {
		_userName = username;
		_serverAddress = serverAddress;
		client = new Client(_userName);
		
	}
	
	void setClient(Client client) {
		this.client = client;
	}

	/**
	 * Logs the client to the server. Any incoming messages from the server will be routed to the provided consumer. If
	 * the client missed any messages while he was offline, all of these will be first routed to client in the order
	 * that they were sent
	 * 
	 * @param messageConsumer The consumer to handle all incoming messages
	 * @param friendshipRequestHandler The callback to handle all incoming friend requests. It accepts the user requesting
	 *        the friendship as input and outputs the reply.
	 * @param friendshipReplyConsumer The consumer to handle all friend requests replies (replies to outgoing
	 *        friends requests). The consumer accepts the user requested and his reply.	
	 */
	public void login(Consumer<InstantMessage> messageConsumer,
			Function<String, Boolean> friendshipRequestHandler,
			BiConsumer<String, Boolean> friendshipReplyConsumer) {
		
		client.<MessageData>startListenLoop(_serverAddress,new MessageDataConsumer(client, messageConsumer, 
				friendshipRequestHandler, friendshipReplyConsumer),MessageData.class);
		
		Optional<List<MessageData>> messages = client.sendAndBlockUntilResponseArrives(new MessageData(ServerTaskType.LOGIN_TASK)
												,new TypeToken<Optional<List<MessageData>>>(){}.getType());
		
		messages.ifPresent(list->list.forEach(new MessageDataConsumer(client, messageConsumer, 
				friendshipRequestHandler, friendshipReplyConsumer)));
	}
	
	/**
	 * Logs the client out, cleaning any resources the client may be using. A logged out client cannot accept any
	 * messages. A client can login (using {@link ClientMsgApplication#login(Consumer, Function, BiConsumer)} after logging out.
	 */
	public void logout() {
		client.send(new MessageData(ServerTaskType.LOGOUT_TASK));
		client.stopListenLoop();
	}
	
	/**
	 * Sends a message to another user
	 * 
	 * @param target The recipient of the message
	 * @param what The message to send
	 */
	public void sendMessage(String target, String what) {
		client.send(new MessageData(ServerTaskType.SEND_MESSAGE_TASK,new InstantMessage(_userName,target,what),target));
	}
	
	/**
	 * Requests the friendship of another user. Friends can see each other online using
	 * {@link ClientMsgApplication#isOnline(String)}. Friend requests are handled similarly to messages. An incoming
	 * friend request is consumed by the friendRequestsConsumer. An incoming friend request <i>reply</i> is consumed by
	 * the friendRequestRepliesConsumer.
	 * 
	 * @param who The recipient of the friend request.
	 */
	public void requestFriendship(String who) {
		client.send(new MessageData(ServerTaskType.REQUEST_FRIENDSHIP_TASK,who));
	}
	
	/**
	 * Checks if another user is online; the client can only ask if friends are online
	 * 
	 * @param who The person to check if he is online
	 * @return A wrapped <code>true</code> if the user is a friend and is offline; a wrapped <code>false</code> if the
	 *         user is a friend and is offline; an empty {@link Optional} if the user isn't a friend of the client
	 */
	public Optional<Boolean> isOnline(String who) {	
		return client.sendAndBlockUntilResponseArrives(new MessageData(ServerTaskType.IS_ONLINE_TASK,who),
				new TypeToken<Optional<Boolean>>(){}.getType());
	}
	
    /**
     * A stopped client does not use any system resources (e.g., messengers).
     * This is mainly used to clean resource use in test cleanup code.
     * You can assume that a stopped client won't be restarted using {@link ClientMsgApplication#login(Consumer, Function, BiConsumer)}
     */
    public void stop() {
            //no need
    }

}
