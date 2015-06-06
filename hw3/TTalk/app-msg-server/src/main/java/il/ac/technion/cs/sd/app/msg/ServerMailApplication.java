package il.ac.technion.cs.sd.app.msg;

import il.ac.technion.cs.sd.lib.clientserver.Server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	private final Set<String> _onlineClients = new HashSet<String>();
	private Map<String, Set<String>> _clientFriends = new HashMap<String, Set<String>>();

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
	
	/**
	 * Starts the server; any previously sent mails, data and indices are loaded.
	 * This should be a <b>non-blocking</b> call.
	 */
	public void start() {
		_server.<Map<String, List<MessageData>>>readObjectFromFile(filename, 
				new TypeToken<Map<String, List<MessageData>>>(){}.getType(), false)
				.ifPresent((data)->_offlineMessages = data);
		_server.<Map<String, Set<String>>>readObjectFromFile(filename, new TypeToken<Map<String, Set<String>>>(){}.getType(), false)
				.ifPresent(data->_clientFriends = data);
		_server.<MessageData>startListenLoop((messageData,from)->{
			switch (messageData._serverTaskType) {
			case LOGIN_TASK:{
				_onlineClients.add(from);
				_server.send(from, _offlineMessages.get(from),true);
				break;
			}
			case SEND_MESSAGE_TASK:{
				if(_onlineClients.contains(messageData._target))
					_server.send(clientAddress, data,false);
				break;
			}
			default:
				break;
			}
		}, MessageData.class);
	}
	
	/**
	 * Stops the server. A stopped server can't accept messages, but doesn't delete any data (messages that weren't received).
	 */
	public void stop() {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * Deletes <b>all</b> previously saved data. This method will be used between tests to assure that each test will
	 * run on a new, clean server. you may assume the server is stopped before this method is called.
	 */
	public void clean() {
		throw new UnsupportedOperationException("Not implemented");
	}
}
