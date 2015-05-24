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

	
	public MessageData(String messageType,
			ArrayList<String> data) {
		this.fromAddress = null;
		this.messageType = messageType;
		this.data = data;
	}
	
	public MessageData(String messageType) {
		this.fromAddress = null;
		this.messageType = messageType;
		this.data = new ArrayList<String>();
	}
	
	MessageData(String fromAddress,
			String messageType,
			ArrayList<String> data) {
		this.fromAddress = fromAddress;
		this.messageType = messageType;
		this.data = data;
	}
	

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public ArrayList<String> getData() {
		return data;
	}

	public void setData(ArrayList<String> data) {
		this.data = data;
	}


	String getFromAddress() {
		return fromAddress;
	}

	void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	
	// Address of the host sending this data message.
	private String fromAddress;
	
	// Represent a user-defined message type.
	private String messageType;
	
	// The data held by this message.
	private ArrayList<String> data;
		
	
	/* A magic-string representing the messageType of a MessageData indicating
	 * the server finished it's task.
	 */
	static final String TASK_ENDED_MESSAGE_TYPE 
		= "9PEdT1SJeR9waQajDF6k";
	
	// serialize this object into byte array (JSON in UTF-8).
	byte[] serialize() 
	{
		Gson gson = new GsonBuilder().create();
		try {
			return gson.toJson(this, GetClassType()).getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException");
		}
	}
	
	// deserialize an MessageData object from a byte array (JSON in UTF-8).
	static MessageData deserialize(byte[] bytes)
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

