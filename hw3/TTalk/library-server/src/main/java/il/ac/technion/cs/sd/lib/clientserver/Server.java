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
	 * Saves a list of objects to persistent memory (file).
	 * @param filename The filename of the file to save, without path.
	 * @param objects The objects to be saved to the file (order is preserved).
	 * Generic object types are not supported.
	 * @param append If true, the new objects are appended to the end of the existing file.
	 * If false, the previous content of the file is lost.
	 * If the file does not already exist - it will be created and this flag is irrelevant.
	 */
	public <T> void saveObjectsToFile(String filename, List<T> objects, boolean append)
	{
		//TODO
	}
	
	/**
	 * Reads a list of objects from persistent memory (file).
	 * @param filename The filename of the file to read, without path.
	 * @param objects The objects to be saved to the file (order is preserved).
	 * Generic object types are not supported.
	 * @return List of read objects (order preserved), or null if no file with that name exists.
	 */
	public <T> Optional<List<T>> readObjectsFromFile(String filename, Class<T> type)
	{
		return null;
		//TODO
	}
	
	
	
	/*
	 * sends the message 'data' 
	 */
	private <T> void send(String clientAddress, T data, int tag)
	{
		
	}
	
}
