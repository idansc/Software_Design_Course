package il.ac.technion.cs.sd.app.msg;

import il.ac.technion.cs.sd.lib.clientserver.Server;

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
	private static final String filename = "serverData";
	Server _server;
	private Map<String, List<MessageData>> _offlineMessages = new HashMap<String, List<MessageData>>();
	private Set<String> _onlineClients = new HashSet<String>();
	private Map<String, List<String>> _clientFriends = new HashMap<String, List<String>>();

    /**
     * Starts a new mail server. Servers with the same name retain all their information until
     * {@link ServerMailApplication#clean()} is called.
     *
     * @param name The name of the server by which it is known.
     */

	public ServerMailApplication(String name) {
		_server = new Server(name);	
	}
	
	/**
	 * @return the server's address; this address will be used by clients connecting to the server
	 */
	public String getAddress() {
		return _server.getAddress();
	}
	
	private void initializeDataFromFile(){
		_server.<Map<String, List<MessageData>>>readObjectFromFile(filename, 
				new TypeToken<Map<String, List<MessageData>>>(){}.getType(), false)
				.ifPresent((data)->_offlineMessages = data);
		_server.<Map<String, List<String>>>readObjectFromFile(filename, new TypeToken<Map<String, Set<String>>>(){}.getType(), false)
				.ifPresent(data->_clientFriends = data);
		_server.<Set<String>>readObjectFromFile(filename,new TypeToken<Set<String>>(){}.getType() , false)
				.ifPresent(data->_onlineClients = data);
	}
	
	private void handleReplyMessageData(String from, MessageData messageData){
		messageData._from = from;
		if(_onlineClients.contains(messageData._target))
			_server.send(messageData._target, messageData, false);
		else if(!_offlineMessages.containsKey(messageData._target))
			_offlineMessages.put(messageData._target,Arrays.asList(messageData));
		else
			_offlineMessages.get(messageData._target).add(0, messageData);
	}
	/**
	 * Starts the server; any previously sent mails, data and indices are loaded.
	 * This should be a <b>non-blocking</b> call.
	 */
	public void start() {
		

		initializeDataFromFile();
		
		_server.<MessageData>startListenLoop((messageData,from)->{
			switch (messageData._serverTaskType) {
			case LOGIN_TASK:{
				_onlineClients.add(from);
				_server.send(from, _offlineMessages.get(from),true);
				break;
			}
			case CLIENT_REPLY_FRIEND_REQUEST_TASK:
				if(messageData._friendRequestAnswer)
					if(!_clientFriends.containsKey(from)){
						assert(!_clientFriends.containsKey(messageData._target));
						_clientFriends.put(from, Arrays.asList(messageData._target));
						_clientFriends.put(messageData._target, Arrays.asList(from));
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
						_server.send(from, Optional.of(_onlineClients.contains(messageData._target)), true);
				_server.send(from, Optional.empty(), true);
				break;
			case LOGOUT_TASK:
				_onlineClients.remove(from);
				break;
			default:
				break;
			}
		}, MessageData.class);
	}
	
	/**
	 * Stops the server. A stopped server can't accept messages, but doesn't delete any data (messages that weren't received).
	 */
	public void stop() {
		_server.saveObjectToFile(filename, _offlineMessages, true);
		_server.saveObjectToFile(filename, _clientFriends, true);
		_server.saveObjectToFile(filename, _onlineClients, true);
		_server.stop();
	}
	
	/**
	 * Deletes <b>all</b> previously saved data. This method will be used between tests to assure that each test will
	 * run on a new, clean server. you may assume the server is stopped before this method is called.
	 */
	public void clean() {
		_server.clearPersistentData();
	}
}
