package il.ac.technion.cs.sd.lib.clientserver;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

// Represents a message one host is sending another.
public class MessageData
{

	// Address of the host sending this data packet.
	public String fromAddress;
	
	// Represent a user-defined packet type.
	public String messageType;
	
	// The data held by this packet.
	public ArrayList<String> data;
	
	
	public MessageData(String fromAddress, String packetType,
			ArrayList<String> data) {
		this.fromAddress = fromAddress;
		this.messageType = packetType;
		this.data = data;
	}
	
	
	/* A magic-string representing the messageType of a MessageData indicating
	 * the server finished it's task.
	 */
	public static final String TASK_ENDED_PACKET_TYPE 
		= "9PEdT1SJeR9waQajDF6k";
	
	// serialize this object into byte array.
	public byte[] serialize() 
	{
		Gson gson = new GsonBuilder().create();
		try {
			return gson.toJson(this, GetClassType()).getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException");
		}
	}
	
	// deserialize an MessageData object from a byte array.
	public static MessageData deserialize(byte[] bytes)
	{
		String json;
		try {
			json = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException");
		}
		Gson gson = new GsonBuilder().create();
		return gson.fromJson(json, GetClassType());
	}
	
	
	private static Type GetClassType()
	{
		return new TypeToken<MessageData>(){}.getType();
	}
}

