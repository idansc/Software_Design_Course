package il.ac.technion.cs.sd.lib.clientserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

//TODO: decide if this should be public.
/**
 * Common utilities.
 */
class Utils {
	
	final static String ENCODING = "UTF-8";
	
	
	/**
	 * Deserializes a UTF-8 GSON string into an object.
	 * @param gsonStr A UTF-8 GSON string 
	 * @param type The type of the object represented by the string.
	 * @return the deserialized object.
	 */
	public static <T> T fromGsonStrToObject(String gsonStr, Type type)
	{
		ByteArrayInputStream is = new ByteArrayInputStream(gsonStr.getBytes());
		
		
		JsonReader reader;
		try {
			reader = new JsonReader(new InputStreamReader(is, ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("bad encoding");
		}
		
		T $ = readObjectFromGsonReader(reader, type);
		try {
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException("Fialed to close reader!");
		}
		return $;
	}
	
	
	/**
	 * Reads from a UTF-8 GSON stream and deserializes into an object.
	 * @return the deserialized object.
	 * @throws RuntimeException on reading failure.
	 */
	public static <T> T readObjectFromGsonReader(JsonReader reader, Type type)
	{		
		Gson gson = new GsonBuilder().create();
		T $;
		try {
			reader.beginArray();
			$ = gson.fromJson(reader, type);
			reader.endArray();
		} catch (IOException e) {
			throw new RuntimeException("IOException");
		}
		return $;
	}
	
	
	/**
	 * Serializes an object into a UTF-8 GSON string.
	 * @param object The object to be serialized.
	 * @return The UTF-8 GSON string representing the object.
	 */
	public static <T> String fromObjectToGsonStr(T object)
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			JsonWriter writer = new JsonWriter(new OutputStreamWriter(os, ENCODING));
		
			writeObjectToJsonWriter(object, writer);
				
			try {
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException("Failed to close writer!");
			}
			
			return new String(os.toByteArray(),ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("bad encoding");
		}

	}
	
	
	/**
	 * Serializes an object into a UTF-8 GSON string.
	 * @param object The object to be serialized.
	 * @return The UTF-8 GSON string representing the object.
	 */
	public static <T> void writeObjectToJsonWriter(T object, JsonWriter writer)
	{
			Gson gson = new GsonBuilder().create();
			try {
				writer.beginArray();
				gson.toJson(object, object.getClass(), writer);
				writer.endArray();
			} catch (IOException e) {
				throw new RuntimeException("IOException");
			}
	}
	
	
	
	//TODO: DELETE
	private static FileWriter logWriter = initLogWriter();
	private static FileWriter initLogWriter()  {
		try {
			return new FileWriter(new File("./TMP__DEBUG__log.txt"));
		} catch (IOException e) {
			throw new RuntimeException("failed to open log to write");
		}
	}
	public static void DEBUG_LOG_LINE(String line)
	{
		synchronized(logWriter)
		{
			try {
				logWriter.write(line + "\n");
				logWriter.flush();
			} catch (IOException e) {
				throw new RuntimeException("failed to write to log");
			}
			
		}
	}
	
	
	public static String showable(String str)
	{
		if (str.length() <= 15)
			return str;
		return str.substring(0, 15);
	}
}
