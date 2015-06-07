package il.ac.technion.cs.sd.app.msg;

import il.ac.technion.cs.sd.lib.clientserver.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.reflect.TypeToken;


/**
 * The server side of the TMail application. <br>
 * This class is mainly used in our tests to start, stop, and clean the server
 */
public class ServerMailApplication {
	private static final String offlineMessagesFileName = "offlineMessages";
	private static final String onlineClientsFileName = "onlineClients";
	private static final String clientFriendsFileName = "clientFriends";
	Server server;
	private Map<String, ArrayList<MessageData>> _offlineMessages = new HashMap<String, ArrayList<MessageData>>();
	private Set<String> _onlineClients = new HashSet<String>();
	private Map<String, ArrayList<String>> _clientFriends = new HashMap<String, ArrayList<String>>();

    /**
     * Starts a new mail server. Servers with the same name retain all their information until
     * {@link ServerMailApplication#clean()} is called.
     *
     * @param name The name of the server by which it is known.
     */

	public ServerMailApplication(String name) {
		server = new Server(name);	
	}
	
	void setServer(Server server) {
		this.server = server;
	}

	/**
	 * @return the server's address; this address will be used by clients connecting to the server
	 */
	public String getAddress() {
		return server.getAddress();
	}
	
	private void initializeDataFromFile(){
		server.<Map<String, ArrayList<MessageData>>>readObjectFromFile(offlineMessagesFileName, 
				new TypeToken<Map<String, List<MessageData>>>(){}.getType())
				.ifPresent((data)->_offlineMessages = data);
		server.<Map<String, ArrayList<String>>>readObjectFromFile(onlineClientsFileName, new TypeToken<Map<String, Set<String>>>(){}.getType())
				.ifPresent(data->_clientFriends = data);
		server.<Set<String>>readObjectFromFile(clientFriendsFileName,new TypeToken<Set<String>>(){}.getType())
				.ifPresent(data->_onlineClients = data);
	}
	
	private void handleReplyMessageData(String from, MessageData messageData){
		messageData._from = from;
		if(_onlineClients.contains(messageData._target))
			server.send(messageData._target, messageData, false);
		else if(!_offlineMessages.containsKey(messageData._target))
			_offlineMessages.put(messageData._target,new ArrayList<>(Arrays.asList(messageData)));
		else{
			_offlineMessages.get(messageData._target).add(messageData);
		
		}
	}
	/**
	 * Starts the server; any previously sent mails, data and indices are loaded.
	 * This should be a <b>non-blocking</b> call.
	 */
	private void dataConsumer(MessageData messageData, String from){
		switch (messageData._serverTaskType) {
		case LOGIN_TASK:{
			_onlineClients.add(from);
			
			server.send(from,
					_offlineMessages.containsKey(from)? Optional.of(_offlineMessages.get(from)) : Optional.empty()
							,true);
			_offlineMessages.remove(from);
			break;
		}
		case CLIENT_REPLY_FRIEND_REQUEST_TASK:
			if(messageData._friendRequestAnswer)
				if(!_clientFriends.containsKey(from)){
					assert(!_clientFriends.containsKey(messageData._target));
					_clientFriends.put(from, new ArrayList<>(Arrays.asList(messageData._target)));
					_clientFriends.put(messageData._target, new ArrayList<>(Arrays.asList(from)));
				}else{
					assert(_clientFriends.containsKey(messageData._target));
					_clientFriends.get(from).add(messageData._target);
					_clientFriends.get(messageData._target).add(from);
				}
		case SEND_MESSAGE_TASK:
		case REQUEST_FRIENDSHIP_TASK:
			handleReplyMessageData(from,messageData);
			break;
		case IS_ONLINE_TASK:
			if(_clientFriends.containsKey(from))
				if(_clientFriends.get(from).contains(messageData._target))
					server.send(from, Optional.of(_onlineClients.contains(messageData._target)), true);
			server.send(from, Optional.empty(), true);
			break;
		case LOGOUT_TASK:
			_onlineClients.remove(from);
			break;
		default:
			break;
		}
	}
	public void start() {
		

		initializeDataFromFile();
		
		server.<MessageData>startListenLoop((messageData,from)->{
			dataConsumer(messageData, from);
		}, MessageData.class);
	}
	
	/**
	 * Stops the server. A stopped server can't accept messages, but doesn't delete any data (messages that weren't received).
	 */
	public void stop() {
		server.saveObjectToFile(offlineMessagesFileName, _offlineMessages);
		server.saveObjectToFile(onlineClientsFileName, _clientFriends);
		server.saveObjectToFile(clientFriendsFileName, _onlineClients);
		server.stop();
	}
	
	/**
	 * Deletes <b>all</b> previously saved data. This method will be used between tests to assure that each test will
	 * run on a new, clean server. you may assume the server is stopped before this method is called.
	 */
	public void clean() {
		server.clearPersistentData();
		_offlineMessages.clear();
		_onlineClients.clear();
		_clientFriends.clear();
	}
}
