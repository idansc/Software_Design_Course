package il.ac.technion.cs.sd.lib.clientserver;

import il.ac.technion.cs.sd.msg.MessengerException;

import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents a client that can communicate (reliably) with a single server.
 * 
 * The server can send a message to a client either synchronically (while it is blocking until a 
 * response from the server is received), or asynchronously. 
 * 
 * Each message sent/received synchronously can consist of an any object type you chose.
 * All messages sent/received asynchronously consist of the same type of your choice.
 * 
 * This class is not thread-safe (meaning you must not access an object of this class from multiple 
 * threads simultaneously). 
 */
public class Client {

	private String _serverAddress;
	private MessengerWrapper _reliableHost;
	
	/**
	 * Creates a new client.
	 * A call to {@link #start(String, Consumer, Type)} must be made before sending/receiving any 
	 * messages with this client.
	 * @param address The address of the new client.
	 */
	public Client(String address)
	{
		_reliableHost = new MessengerWrapper(address);
	}
	
	/**
	 * returns the address of this client.
	 */
	public String getAddress()
	{
		return _reliableHost.getAddress();
	}
	
	
	/**
	 * Starts listening for incoming messages from the server. You can't use the client for any
	 * communication before starting it. 
	 * After the client is started, each message received from the server invokes the consumer's 
	 * callback function.
	 * @param serverAddress The server's address.
	 * @param consumer The consumer who's callback will be invoked for each message received from the server.
	 * While the consumer's callback is running - the listen loop is frozen, so the code in the 
	 * callback shouldn't wait for a new message to be received by this client. 
	 * @param dataType The type of the object the server sends asynchronously to the client in each 
	 * message as data.
	 * (i.e. the type of the parameter the consumer's callback function receives).
	 * e.g.:
	 * 		Integer.class
	 * If the type is generic, for example, a list of Integers, you should pass as 'dataType' 
	 * something created with the following pattern:
	 * 		{@code new TypeToken<List<Integer>>(){}.getType())}
	 * (sorry for that - it's a requirement by underlying GSON library).

	 * If the type is generic, for example, a list of Integers, you should pass as 'dataType' 
	 * something created with the following pattern:
	 * {@code new TypeToken<List<Integer>>(){}.getType())}
	 * @throws InvalidMessage Invalid message received from the server 
	 * For example: the object sent as message data was not of type 'type'.
	 * @throws InvalidOperation When the listen loop is already running when calling this method.
	 */
	public <T> void start(String serverAddress, Consumer<T> consumer, Type dataType)
	{
		String originalServerAddress = _serverAddress;
		_serverAddress = serverAddress;
		
		try {
			_reliableHost.start((fromAddress,data) -> {
				consumer.accept(Utils.fromGsonStrToObject(data, dataType));
			});
		} catch (MessengerException e) {
			_serverAddress = originalServerAddress;
			throw new CommunicationFailure();
		}
		
	}
	
	
	/**
	 * Stop the listen loop of the client (messages sent from server will no longer be consumed.
	 * @throws InvalidOperation When the listen loop was not running when calling this method.
	 */
	public void stopListenLoop()
	{
		_reliableHost.stop();
	}

	/**
	 * Sends a message to the server.
	 * @param data The object to be sent to the server (as message data).
	 * Parametric types of data are not supported.
	 */
	public <T> void send(T data) {
		try {
			String payload = Utils.fromObjectToGsonStr(data);
			_reliableHost.send(_serverAddress, payload, false);
		} catch (MessengerException e) {
			throw new InvalidOperation();
		} 
	}
	
	
	/**
	 * Sends a message to the server, and blocks until a response message is received.
	 * @param data The object to be sent to the server (as message data).
	 * @param responseType The type of the object the server sends back as data.
	 * (i.e. the type of the returned value).
	 * @return The response message data. 
	 * The response message is guaranteed to be the response to the message sent by this method
	 * (and not some other unrelated message the server sent the client).
	 * @throws InvalidMessage Invalid message was received back from the server. 
	 */
	public <T, S> S sendAndBlockUntilResponseArrives(T data, Type responseType)
	{
		try {
			String str = _reliableHost.sendAndBlockUntilResponseArrives(
					_serverAddress, Utils.fromObjectToGsonStr(data));
			
			return Utils.fromGsonStrToObject(str, responseType);
		} catch (MessengerException e) {
			throw new InvalidOperation();
		}
		
	}
	
	@Override
	public String toString() {

		return _reliableHost.getAddress();
	}
	
}
