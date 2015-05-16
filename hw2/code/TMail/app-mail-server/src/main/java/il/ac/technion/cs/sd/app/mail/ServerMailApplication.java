package il.ac.technion.cs.sd.app.mail;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import il.ac.technion.cs.sd.lib.clientserver.Server;

/**
 * The server side of the TMail application. <br>
 * This class is mainly used in our tests to start, stop, and clean the server
 */
public class ServerMailApplication {
	private Server _server;
	List<Mail> mailList;
	
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
		return _server.getserverAddress();
	}
	
	/**
	 * Starts the server; any previously sent mails, data and indices under this server name are loaded. It is possible
	 * to start a new server instance in same, or another process. You may assume that two server instances with the
	 * same name won't be in parallel. Similarly, {@link ServerMailApplication#stop()} will be called before subsequent
	 * calls to {@link ServerMailApplication#start()}.
	 */
	public void start() {
//TODO
		try {
			List<String> serverData = Files.readAllLines(Paths.get(getAddress() + ".txt"),
					Charset.defaultCharset());
		} catch (IOException e) {
			throw new RuntimeException();
		}
	    
	}
	
	/**
	 * Stops the server. A stopped server can't accept mail, but doesn't delete any data. A stopped server does not use
	 * any system resources (e.g., messengers).
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
	
	
	private ArrayList<Mail> readAllMailsFromFile(String filename) throws IOException
	{
		File file = new File(filename);
		return readAllMailsFromStream(new FileInputStream(file));
	}
	
	private ArrayList<Mail> readAllMailsFromStream(InputStream stream) throws IOException
	{
		Gson gson = new GsonBuilder().create();
		JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
		
		
		ArrayList<Mail> $ = new ArrayList<Mail>();
		reader.beginArray();
		while (reader.hasNext()) {
            Mail mail = gson.fromJson(reader, Mail.class);
            $.add(mail);
        }
		
		reader.endArray();
        reader.close();
		
		return $;
		
	}
	
	/* mail will be appended to outputFile */
	private void appendMailToFile(Mail mail, String outputFile) throws IOException
	{
		File file = new File(outputFile);
		OutputStream stream = new FileOutputStream(file, true);
		writeMailToStream(mail,stream);
		stream.flush();
		stream.close();
		
	}
	
	private void writeMailToStream(Mail mail, OutputStream stream) throws IOException
	{
		Gson gson = new GsonBuilder().create();
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(stream, "UTF-8"));
		writer.beginArray();
		gson.toJson(mail, Mail.class, writer);
		writer.endArray();
        writer.close();
		
	}
}
