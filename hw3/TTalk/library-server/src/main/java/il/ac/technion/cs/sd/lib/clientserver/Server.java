package il.ac.technion.cs.sd.lib.clientserver;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class Server {

	private String _address;
	
	/*
	 * @param address - the address of the new server.
	 */
	public Server(String address)
	{
		_address = address;
	}


	/**
	 *  
	 * Starts the server listen loop. 
	 * While listening, the server process incoming messages from clients.
	 * Each incoming message invokes a callback function. 
	 * TODO
	 * @param <T> The type of the incoming message . 
	 * @param consumer The callback function incoming requests from the clients.
	 */
	public <T> void start(BiConsumer<T,String> callback, Class<T> answerType) {
		
		//TODO
	}
	
	public void stop()
	{
		//TODO
	}
	
	

	/**
	 * Sends a message to a client.
	 * @param clientAddress The address of the client.
	 * @param data The data to be sent to the client.
	 * Generic types of 'data' are not supported.
	 */
	public <T> void send(String clientAddress, T data)
	{
		
	}
	
	
	/**
	 * Saves a list of objects to persistent memory (file).
	 * @param filename The filename of the file to save, without path.
	 * If the file already exists, the previous content is lost.
	 * @param objects The objects to be saved to the file (order is preserved).
	 * Generic object types are not supported.
	 */
	public <T> void saveObjectsToFile(String filename, List<T> objects)
	{
		//TODO
	}
	
	/**
	 * Reads a list of objects from persistent memory (file).
	 * @param filename The filename of the file to read, without path.
	 * @param objects The objects to be saved to the file (order is preserved).
	 * Generic object types are not supported.
	 * @return List of read objects (order preserved), or empty value if no file with that name exists.
	 */
	public <T> Optional<List<T>> readObjectsFromFile(String filename, Class<T> type)
	{
		return null;
		//TODO
	}

	
}
