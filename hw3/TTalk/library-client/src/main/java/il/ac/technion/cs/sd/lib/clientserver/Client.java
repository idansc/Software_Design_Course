package il.ac.technion.cs.sd.lib.clientserver;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Client {

	private String _address;
	
	/**
	 * 
	 * @param address The address of the client.
	 */
	public Client(String address)
	{
		_address = address;
	}
	
	
	/**
	 * Starts the listen loop of the client.
	 * During the listen loop - messages are received from the server.
	 * Each message received from the server invokes a consumer's callback function.
	 * @param consumer The consumer who's callback will be invoked for each message received from the server. 
	 * @param type The type of the object the server sends the client in each message as data.
	 * (i.e. the parameter type of the consumer's callback function).
	 * Generic types are not supported.
	 * @throws InvalidMessage Invalid message received from the server (the object sent as data was 
	 * not of type 'type').
	 */
	public <T> void start(Consumer<T> consumer, Class<T> type)
	{
		 //TODO
	}
	
	public void stop()
	{
		//TODO
	}
	
	public <T> void send(String serverAddress, T data) {
		//TODO
	}
	
	public <RequestType, AnswerType> AnswerType sendAndBlock(
			String serverAddress, RequestType data, Class<AnswerType> answerType)
	{
		//TODO
		return null;
	}

	public class InvalidMessage extends RuntimeException {private static final long serialVersionUID = 1L;}
}
