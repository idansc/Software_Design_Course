package il.ac.technion.cs.sd.lib.clientserver;

import il.ac.technion.cs.sd.msg.MessengerException;

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
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.io.FileUtils;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;



public class ServerLib {

	private MessengerWrapper messenger;
	
	
	public String getAddress() {
		return messenger.getAddress();
	}

	public ServerLib(String address)
	{
		messenger = new MessengerWrapper(address);
	}

	public <T> void start(BiConsumer<T,String> consumer, Type dataType) {
		
		try {
			messenger.start((fromAddress, data) -> {
				consumer.accept(Utils.fromGsonStrToObject(data, dataType), fromAddress);
			});
		} catch (MessengerException e) {
			System.out.println(e.getMessage());
			throw new InvalidOperation();
		}
		
	}

	public void stop()
	{
		messenger.stop();
	}
	

	public <T> void send(String clientAddress, T data, boolean isResponse)
	{
		try {
			messenger.send(clientAddress, Utils.fromObjectToGsonStr(data), isResponse);
		} catch (MessengerException e) {
			throw new CommunicationFailure();
		}
	}
	
	
	public void clearPersistentData()
	{
		File persistentDataDir = getServerPersistentDir();
		if (!persistentDataDir.exists())
		{
			return;
		}
		try {
			if (!persistentDataDir.exists())
				return;
				FileUtils.cleanDirectory(getServerPersistentDir());
				FileUtils.deleteDirectory(getServerPersistentDir());
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	

	public <T> void saveObjectToFile(String filename, T data)
	{
		File file = getFileByName(filename, true);
		
		try (JsonWriter persistentDataWriter = createJsonWriter(file)) 
		{
			Utils.writeObjectToJsonWriter(data,persistentDataWriter);
		} catch (IOException e) {			
			throw new RuntimeException("Failed to close stream");
		}
	}
	 
	
	public <T> Optional<T> readObjectFromFile(String filename, Type type)
	{
		File file = getFileByName(filename, false);
		
		if (!file.exists())
		{
			return Optional.empty();
		}
		
		
		Optional<T> $;
		
		try (JsonReader persistentDataReader = createJsonReader(file))
		{
			$ =  Optional.of(Utils.readObjectFromGsonReader(persistentDataReader, type));
		}
		catch (RuntimeException e)
		{
			throw new BadFileContent();
		} catch (IOException e1) {
			throw new RuntimeException("Failed to close stream");
		}
		
		return $;
	}
	
	private JsonWriter createJsonWriter(File f)
	{
		try {
			OutputStream stream = new FileOutputStream(f);
			return  new JsonWriter(new OutputStreamWriter(stream, Utils.ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException");
		} catch (FileNotFoundException e) {
			throw new RuntimeException("FileNotFoundException");
		}
	}
	
	private JsonReader createJsonReader(File f)
	{
		if(!f.exists())
		{
			throw new RuntimeException("File does not exist");
		}
		try {
			InputStream stream = new FileInputStream(f);
			return new JsonReader(new InputStreamReader(stream, Utils.ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException");
		} catch (FileNotFoundException e) {
			throw new RuntimeException("FileNotFoundException");
		}
	}

	private File getFileByName(String filename, boolean createItsDirIfNecessary)
	{
		File serverDir = getServerPersistentDir();
		if (createItsDirIfNecessary)
		{
			serverDir.mkdirs();
		}
		return new File(serverDir, filename);
	}

	private File getServerPersistentDir() {
		return new File(getPesistentDirOfAllServers(), getServerPersistentDirName());
	}
	
	private String getServerPersistentDirName()
	{
		return Integer.toString(getAddress().hashCode());
	}
	
	private static File getPesistentDirOfAllServers()
	{
		return new File("./TMP___ServersData");
	}
	
	@SuppressWarnings("serial")
	public class BadFileContent extends RuntimeException {}
	
}
