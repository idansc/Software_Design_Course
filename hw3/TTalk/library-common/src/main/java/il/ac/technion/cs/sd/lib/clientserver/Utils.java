package il.ac.technion.cs.sd.lib.clientserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

//TODO: decide if this should be public at all.
/**
 * Common utilities.
 */
public class Utils {
	
	private final String ENCODING = "UTF-8";
	
	
	/**
	 * Deserializes a UTF-8 GSON string into an object.
	 * @param gsonStr A UTF-8 GSON string 
	 * @param type The type of the object represented by the string.
	 * @return the deserialized object.
	 */
	public static <T> T fromGsonStrToObject(String gsonStr, Class<T> type)
	{
		ByteArrayInputStream is = new ByteArrayInputStream(gsonStr.getBytes());
		
		Gson gson = new GsonBuilder().create();
		
		JsonReader reader;
		try {
			reader = new JsonReader(new InputStreamReader(is, ENCODING));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("bad encoding");
		}
		
		return gson.fromJson(reader, type);
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
			Gson gson = new GsonBuilder().create();
			gson.toJson(object, object.getClass(), writer);
			return new String(os.toByteArray(),ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("bad encoding");
		}

	}
	
	
}
