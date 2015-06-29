package il.ac.technion.cs.sd.lib;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


public class MessengerWrapper {
	final static String ENCODING = "UTF-8";
	
	private String address;
	protected Messenger me;
	protected final BlockingQueue<Boolean> ack; 
	public static final String ack_mssg = "";
	protected final BlockingQueue<String> responds;
	
	private BiConsumer<String, String> consumer;
	private BlockingQueue<PayLoad> responseBQ = new LinkedBlockingQueue<PayLoad>();
	Long currentMessageConsumedId;	
	Object consumptionLock = new Object();
	Long nextMessageIdToGive = 0L;	
	private static final int MAX_TIME_FOR_SUCCESFUL_DELIVERY = 100; 
	private boolean messageRecivedIndicator = false;
	Object sendingLock = new Object();
	
	boolean messageLoopRequestedToStop = false;
	boolean messageLoopCurrentlyRunning = false;
	BlockingQueue<String> primitiveMessagesToHandle = new LinkedBlockingQueue<>();
	Thread listenThread;

	
	/**
	 * sending data from messengerWrapper always successful
	 * keep sending data until ack
	 * @param address
	 * @param dedicatedConsumer
	 */
	public MessengerWrapper(String address,final Consumer<String> dedicatedConsumer) {
		this.address = address;
		ack = new LinkedBlockingQueue<>();
		responds = new LinkedBlockingQueue<>();
		BiConsumer<Messenger,String> messengerConsumer = (myMessenger,json)->{
			if (json.equals(ack_mssg)){
				ack.add(true);
				return;
			}
			PayLoad tmp=PayLoad.fromString(json);
			try {
				myMessenger.send(tmp.sender,ack_mssg);
			} catch (MessengerException e) {
				throw new RuntimeException(e);
			}
			if (tmp.respond){
				responds.add(tmp.payload);
				return;
			}
			dedicatedConsumer.accept(tmp.payload);
		};
		try {
			me = new MessengerFactory().start(address, messengerConsumer);
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	public MessengerWrapper(String address)
	{
		this(address,null);
	}
			
	
	void start(BiConsumer<String, String> consumer) throws MessengerException
	{
		if (messageLoopCurrentlyRunning)
		{
			throw new InvalidOperation();
		}
		
		this.consumer = consumer;
		
		me = new MessengerFactory().start(address, payload -> {

				
				if (payload.isEmpty())
				{
					messageRecivedIndicator = true;
					return;
				} 
				
				sendRecipeintConfirmation(payload);
				
				PayLoad message = getInnerMessageFromPayload(payload);
				if (message.responseTargetId != null)
				{
					try {
						responseBQ.put(message);
						assert(responseBQ.size() <= 2); // see documentation of responseBQ
					} catch (InterruptedException e) {
						throw new RuntimeException("InterruptedException");
					}
					return;
				}
				try {
					primitiveMessagesToHandle.put(payload);
				} catch (Exception e) {
					throw new RuntimeException("failded to put in primitiveMessagesToHandle");
				}
		});
		
		listenThread = new Thread(() -> {
				listeningLoopOnDedicatedThread();//TODO:rename
		});
		
		listenThread.start();
		
		while (!messageLoopCurrentlyRunning)
		{
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException("InterruptedException");
			}
		}
		
	}
	
	void stop()
	{
		if (!messageLoopCurrentlyRunning)
		{
			throw new InvalidOperation();
		}
		try {
			Thread.sleep(MAX_TIME_FOR_SUCCESFUL_DELIVERY);
		} catch (InterruptedException e1) {
			throw new RuntimeException("InterruptedException");
		}
		
		messageLoopRequestedToStop = true;
		
		while (messageLoopCurrentlyRunning)
		{
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException("InterruptedException");
			}
		}
		try {
			me.kill();
		} catch (MessengerException e) {
			throw new RuntimeException("failed to kill messenger!");
		}
		responseBQ.clear();
		currentMessageConsumedId = null;
	}
	
	
	/**
	 * send data always successful
	 * don't use from the onRecieve consumer
	 * @param to
	 * @param payload
	 */
	public synchronized void dedicatedBlockingSend(String to,String payload, boolean respond){
		// try to resend every 100 milliseconds until ack 
		try {
			do {
				me.send(to, new PayLoad(payload, me.getAddress(), respond).toJson());
			} while (ack.poll(100, TimeUnit.MILLISECONDS) == null);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	void send(String targetAddress, String data, boolean isResponse) 
			throws MessengerException
	{
		if (isResponse)
		{
			if (currentMessageConsumedId == null)
			{
				throw new InvalidOperation();
			}
			send(targetAddress, data, currentMessageConsumedId, null);
		} else
		{
			send(targetAddress, data, null, null);
		}
	}
	
	String sendAndBlockUntilResponseArrives(
			String  targetAddress, String data) throws MessengerException
	{
		if (!messageLoopCurrentlyRunning)
		{
			throw new InvalidOperation();
		}
		
		long responseRequestorId;
		synchronized(nextMessageIdToGive)
		{
			responseRequestorId = nextMessageIdToGive;
			nextMessageIdToGive++;
		}
		
		
		send(targetAddress, data, null, responseRequestorId);
		
		PayLoad response;
		
		while (true)
		{
			try {
				response = responseBQ.take();
			} catch (InterruptedException e) {
				throw new RuntimeException("InterruptedException");
			}
			if (response.responseTargetId == responseRequestorId)
			{
				return response.payload;
			}
			try {
				responseBQ.put(response);
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new RuntimeException("InterruptedException");
			} 
		}
		
	}

	private void newMessageArrivedCallback(String data)
	{
		PayLoad message = getInnerMessageFromPayload(data);
		currentMessageConsumedId = message.messageId;
		consumer.accept(message.sender, message.payload);
		currentMessageConsumedId = null;
			
	}

	
	private void send(String  targetAddress, String data, Long respnseTargetId, Long newMessageId) 
			throws MessengerException
	{
		if (!messageLoopCurrentlyRunning)
		{
			throw new InvalidOperation();
		}
		
		if (newMessageId == null)
		{
			synchronized(nextMessageIdToGive)
			{
				newMessageId = nextMessageIdToGive;
				nextMessageIdToGive++;
			}
		}
		PayLoad newMessage = new PayLoad(newMessageId,respnseTargetId, data, address);
		synchronized (sendingLock)
		{
			assert(!messageRecivedIndicator);
			while (!messageRecivedIndicator)
			{
				String payload = fromObjectToGsonStr(newMessage);
				
				primitiveSendRepeatedly(targetAddress, payload);
				try {
					Thread.sleep(MAX_TIME_FOR_SUCCESFUL_DELIVERY);
				} catch (InterruptedException e) {
					throw new RuntimeException("InterruptedException");
				}
			}
			messageRecivedIndicator = false;
		}
	}

	private void primitiveSendRepeatedly(String to, String payload)
	{
		assert(to != null && payload != null);	
		while (true)
		{	
			try {
				me.send(to, payload);
				return;
			} catch (MessengerException e) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {
					assert(false);
				}
			}
		}
	}

	private void listeningLoopOnDedicatedThread()
	{
		messageLoopCurrentlyRunning = true;
		while (!messageLoopRequestedToStop)
		{
			String str;
			try {
				str = primitiveMessagesToHandle.poll(10, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException("InterruptedException");
			}
			if (str != null)
			{
				newMessageArrivedCallback(str);
			}
		}
		messageLoopRequestedToStop = false;
		messageLoopCurrentlyRunning = false;
	}
	
	private void sendRecipeintConfirmation(String payload)
	{
		PayLoad m = getInnerMessageFromPayload(payload);
		primitiveSendRepeatedly(m.sender, "");
	}

	private PayLoad getInnerMessageFromPayload(String payload) {
		return fromGsonStrToObject(payload, PayLoad.class);
	}
	
	
	/**
	 * send from inside a messenger consumer
	 * have a bigger overhead
	 */
	public void dedicatedSendFromConsumer(String to,String payload){
		try {
			do {
				me.send(to, new PayLoad(payload, me.getAddress(), false).toJson());
			} while (notAck(me.getNextMessage(100)));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean notAck(String string){
		return !ack_mssg.equals(string);
	}
	
	public String getAddress() {
		return address;
	}
	
	public class InvalidOperation extends RuntimeException {private static final long serialVersionUID = 5911133254940179859L;}

	
	

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


	
}


