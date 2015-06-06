package il.ac.technion.cs.sd.lib.clientserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

//TODO: add documentation to the package.
//TODO: compile to html javadoc.
//TODO: consider giving in javadoc a tip on sending a type for generic types.
//TODO: add to documentation each time there's a "Type" - the generic pattern.

/**
 * Represents a server that can communicate (reliably) with multiple clients, and save/load 
 * persistent data.
 * The messages sent and received consist of objects of any type.
 * 
 * This class is not thread-safe (meaning you must not access an object of this class from multiple 
 * threads simultaneously). 
 */
public class Server {

	private String _address;
	
	private JsonReader persistentDataReader;
	private JsonWriter persistentDataWritter;
	
	
	public String getAddress() {
		return _address;
	}


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
	 * @param consumer The consumer who's callback will be invoked for each message received from a client.
	 * Any message sent back to the client via @{link {@link #sendResponse(String, Object)} 
	 * from the callback function is considered by the client as a response to the specific message 
	 * that invoked the callback.
	 * While the consumer's callback is running - the listen loop is frozen, so the code in the 
	 * callback shouldn't wait for a new message to arrive. 
	 * @param dataType The type of the object sent by the client in each message
	 * (i.e., the type of the object passed to the consumer's callback function).
	 * If the type is generic, for example, a list of Integers, you should pass as 'dataType' 
	 * something created with the following pattern:
	 * {@code new TypeToken<List<Integer>>(){}.getType())}
	 * @throws InvalidMessage If the listen loop is already running. 
	 */
	public <T> void startListenLoop(BiConsumer<T,String> consumer, Type dataType) { //TODO
		
		//TODO
	}

	

	/**
	 * Stop the listen loop (messages sent from clients will no longer be consumed.
	 * @throws InvalidOperation When the listen loop was not running when calling this method.
	 */
	public void stop()
	{
		//TODO
	}
	
	

	/**
	 * Sends a message to a client.
	 * @param clientAddress The address of the client.
	 * @param data The data to be sent to the client.
	 * @param isResponse true iff 'data' is a response to a message previously sent by the client.
	 * When true, you must call this method only from the consumer of the listen loop (i.e., from the 
	 * callback function invoked by the message to which the response is for).
	 */
	public <T> void send(String clientAddress, T data, boolean isResponse)
	{
		//TODO
	}
	
	
	/**
	 * Saves a list of objects to persistent memory (file).
	 * @param filename The filename, without path, of the file to save 'data' into.
	 * @param data The object to be saved to the file.
	 * @param append If true 'data' is appended to the end of the file (if already exists).
	 * If false, the previous content of the file (if already exists) is lost.   
	 */
	public <T> void saveObjectToFile(String filename, T data, boolean append)
	{
		
		OutputStream out = 
				_persistentConfig.getPersistentMailOverwriteOutputStream();
		
		if (out == null)
			return;
		
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		Gson gson = new GsonBuilder().create();
		writer.beginArray();
		
		Iterator<Mail> it = allMail.descendingIterator();
		while (it.hasNext())
		{
			Mail mail = it.next(); 
			DoublyLinkedList<Mail>.Node tmp = mail.newMailNode;
			mail.newMailNode = null;
			
			gson.toJson(mail, Mail.class, writer);
			
			mail.newMailNode = tmp;
		}	
		writer.close();
	}
	 
	/**
	 * Reads a list of objects from persistent memory (file).
	 * @param filename The filename of the file to read, without path.
	 * @param objects The objects to be saved to the file (order is preserved).
	 * @param startFromStart If true, the reading starts from the beginning of the file.
	 * If false, we read the next object in the file.
	 * @return The object read, or empty if we've already read all objects, or the file doesn't exist.
	 */
	public <T> Optional<T> readObjectFromFile(String filename, Type type, boolean readFromStart) //TODO
	{
		File file = getFileByNameAndCreateItsDirIfNecessary(filename);
		if (!file.exists())
		{
			return Optional.empty();
		}
		
		if (readFromStart)
		{
			if (persistentDataReader != null)
			{
				persistentDataReader.close();
			}
			try {
				persistentDataReader = new JsonReader(new FileInputStream(file));
				
				persistentDataInputStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				assert(false);
			}
		} else
		{
			if (persistentDataInputStream == null)
			{
				try {
					persistentDataInputStream = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					assert(false);
				}
			}
		}
		
		try{
			return Utils.fromGsonStreamToObject(persistentDataInputStream, type);
		} catch (RuntimeException e)
		{
			return Optional.empty();
		}
	}
	

	

	/**
	 * Precondition: the directory f is in must already exist (f itself might not exist).
	 */
	private JsonWriter createJsonWriter(File f)
	{
		try (OutputStream stream = new FileOutputStream(f)){
			return new JsonWriter(new OutputStreamWriter(stream, Utils.ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException");
		} catch (IOException e1) {
			throw new RuntimeException("Failed to close stream!");
		} 
	}
	
	
	/**
	 * Precondition: f is an existing file.
	 */
	private JsonReader createJsonReader(File f) throws FileNotFoundException
	{
		assert(f.exists());
		try (InputStream stream = new FileInputStream(f)){
			return new JsonReader(new InputStreamReader(stream, Utils.ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException");
		} catch (IOException e1) {
			throw new RuntimeException("Failed to close stream!");
		}
	}

	/**
	 * Returns a File object representing the file.
	 * If the directory of the file does not exist - it is created (along with all necessary parents).
	 * @param filename - the filename, without path.
	 */
	private File getFileByNameAndCreateItsDirIfNecessary(String filename)
	{
		File serverDir = new File(getPesistentDirOfAllServers(), getServerDirName());
		serverDir.mkdirs();
		return new File(serverDir, filename);
	}
	
	// returns the unique name (wihtout path) of the directory holding the persistent files of the server. 
	private String getServerDirName()
	{
		return Integer.toString(getAddress().hashCode());
	}
	
	private static File getPesistentDirOfAllServers()
	{
		return new File("./TMP___ServersData");
	}
}
