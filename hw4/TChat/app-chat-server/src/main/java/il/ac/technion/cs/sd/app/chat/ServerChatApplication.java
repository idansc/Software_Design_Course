package il.ac.technion.cs.sd.app.chat;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.gson.reflect.TypeToken;

import il.ac.technion.cs.sd.app.chat.RoomAnnouncement.Announcement;
import il.ac.technion.cs.sd.lib.ServerLib;


/**
 * The server side of the TMail application. <br>
 * This class is mainly used in our tests to start, stop, and clean the server
 */
public class ServerChatApplication {

	private ServerLib server;
	private static final String onlineClientsFileName = "onlineClients";
	private static final String roomsToClientFileName = "roomsToClient";
	private static final String clientsToRoomsFileName = "clientsToRooms";
	private Set<String> _onlineClients = new HashSet<String>();
	private Map<String, Set<String>> _roomsToClient = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> _clientsToRooms = new HashMap<String, Set<String>>();
    /**
     * Starts a new mail server. Servers with the same name retain all their information until
     * {@link ServerChatApplication#clean()} is called.
     *
     * @param name The name of the server by which it is known.
     */

	public ServerChatApplication(String name) {
		server = new ServerLib(name);
	}
	
	/**
	 * @return the server's address; this address will be used by clients connecting to the server
	 */
	public String getAddress() {
		return server.getAddress();
	}
	
	
	private void messageConsumer(ServerMessage messageData, String from){
		switch (messageData.serverTaskType) {
		case LOGIN:{
			_onlineClients.add(from);
			if(_clientsToRooms.containsKey(from))
				_clientsToRooms.get(from).forEach(room->{
					if(!_roomsToClient.containsKey(room))
						_roomsToClient.put(room, new HashSet<>(Arrays.asList(from)));
					else{						
						sendAnnouncment(from, room, Announcement.JOIN);
						_roomsToClient.get(room).add(from);
					}
				});;
			break;
		}
		case SEND_MESSAGE:{
			if(!_roomsToClient.containsKey(messageData.chatMessage.room))
				server.send(from, Optional.empty(), true);
			else{
				_roomsToClient.get(messageData.chatMessage.room).stream().filter(client->!client.equals(from)).forEach(client->server.send(client,
						 new ClientMessage(TaskClientType.MESSAGE).setMessage(messageData.chatMessage),false));
				server.send(from, Optional.of(true), true);
			}
			break;
		}
		case JOIN_ROOM:{
			String room = messageData.room;
			if(!_roomsToClient.containsKey(room))				
				_roomsToClient.put(room, new HashSet<>(Arrays.asList(from)));
			else{
				if(!_roomsToClient.get(room).add(from)){
					server.send(from, Optional.empty(), true);
					break;
				}
				sendAnnouncment(from, room, Announcement.JOIN);
			}
			if(!_clientsToRooms.containsKey(from))
				_clientsToRooms.put(from,  new HashSet<>(Arrays.asList(room)));		
			else
				_clientsToRooms.get(from).add(room);
			server.send(from, Optional.of(true), true);				
			break;

		}
		case LEAVE_ROOM:{
			String room = messageData.room;
			if(!_onlineClients.contains(from) ||
					!_roomsToClient.containsKey(room) 
					|| !_roomsToClient.get(room).contains(from))
				server.send(from, Optional.empty(), true);
			else{
				_roomsToClient.get(room).remove(from);
				if(_roomsToClient.get(room).isEmpty()){
					_roomsToClient.remove(room);
				}
				else{
					sendAnnouncment(from, room, Announcement.LEAVE);					
				}
				_clientsToRooms.get(from).remove(room);
				server.send(from, Optional.of(true), true);
			}
			break;
				
		}
		case LOGOUT:{
			if(_onlineClients.contains(from)){
				if (_clientsToRooms.containsKey(from))
				{
					_clientsToRooms.get(from).forEach(room->{
						_roomsToClient.get(room).remove(from);
						if(_roomsToClient.get(room).isEmpty()){
							_roomsToClient.remove(room);
						}
					});
				}
				_onlineClients.remove(from);
				if(!_onlineClients.isEmpty())
					_clientsToRooms.get(from).stream().filter(room->_roomsToClient.containsKey(room))
					.forEach(room->sendAnnouncment(from, room, Announcement.DISCONNECT));
				break;
			}
		}
		case GET_JOINED_ROOM:
			List<String> $ = _clientsToRooms.containsKey(from)? new ArrayList<String>(_clientsToRooms.get(from)):Collections.emptyList();
			server.send(from, Optional.of($) , true);
			break;
		case GET_ALL_ROOMS:
			server.send(from, Optional.of(new ArrayList<>(_roomsToClient.keySet())), true);
			break;
		case GET_CLIENTS_IN_ROOM:{
			if(!_roomsToClient.containsKey(messageData.room))
				server.send(from, Optional.empty(), true);
			else
				server.send(from, Optional.of(new ArrayList<>(_roomsToClient.get(messageData.room))), true);
			break;
		}
		default:
			break;
		}
	}

	private void sendAnnouncment(String from, String room, Announcement type) {
		_roomsToClient.get(room).stream().filter(client->!client.equals(from)).forEach(client->server.send(client,
					new ClientMessage(TaskClientType.ANNOUNCEMENT).setAnnouncement(new RoomAnnouncement(from, room, type)),
					false));
	}

	/**
	 * Starts the server; any previously sent mails, data and indices are loaded.
	 * This should be a <b>non-blocking</b> call.
	 */
	public void start() {
		server.<HashSet<String>>readObjectFromFile(onlineClientsFileName, new TypeToken<HashSet<String>>(){}.getType())
		.ifPresent(data->_onlineClients = data);
		server.<HashMap<String, Set<String>>>readObjectFromFile(roomsToClientFileName,new TypeToken<HashMap<String, Set<String>>>(){}.getType())
				.ifPresent(data->_roomsToClient = data);	
		server.<HashMap<String, Set<String>>>readObjectFromFile(clientsToRoomsFileName,new TypeToken<HashMap<String, Set<String>>>(){}.getType())
		.ifPresent(data->_clientsToRooms = data);
		server.start(this::messageConsumer, ServerMessage.class);
	}
	
	/**
	 * Stops the server. A stopped server can't accept messages, but doesn't delete any data (messages that weren't received).
	 */
	public void stop() {
		server.saveObjectToFile(onlineClientsFileName, _onlineClients);
		server.saveObjectToFile(roomsToClientFileName, _roomsToClient);
		server.saveObjectToFile(clientsToRoomsFileName, _clientsToRooms);
		_onlineClients.clear();
		_clientsToRooms.clear();
		_roomsToClient.clear();
		server.stop();
	}
	
	/**
	 * Deletes <b>all</b> previously saved data. This method will be used between tests to assure that each test will
	 * run on a new, clean server. you may assume the server is stopped before this method is called.
	 */
	public void clean() {
		server.clearPersistentData();
		_onlineClients.clear();
		_clientsToRooms.clear();
		_roomsToClient.clear();
	}
	
	

	// for testing.
	void setServer(ServerLib server) {
		this.server = server;
	}
}