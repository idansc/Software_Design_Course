package il.ac.technion.cs.sd.lib.clientserver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

class MessengerWrapper {

	private String address;
	private Messenger me;
	
	public String getAddress() {
		return address;
	}
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

	//TODO
	private class PayLoad
	{
		@SuppressWarnings("unused")
		PayLoad() {}
		PayLoad(long messageId, Long respnseTargetId, String payload, String sender) {
			this.messageId = messageId;
			this.responseTargetId = respnseTargetId;
			this.payload = payload;
			this.sender = sender;
		}

		Long messageId;
		Long responseTargetId; 
		
		String payload;
		String sender;
	}

	MessengerWrapper(String address)
	{
		this.address = address;
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
					Utils.DEBUG_LOG_LINE("\"\"   _address=" + Utils.showable(address)); 
					
					assert(!messageRecivedIndicator);
					messageRecivedIndicator = true;
					
					return;
				} 
				
				sendRecipeintConfirmation(payload);
				
				PayLoad message = getInnerMessageFromPayload(payload);
				if (message.responseTargetId != null)
				{
					Utils.DEBUG_LOG_LINE("---responseBQ.put: " + message);
					
					try {
						responseBQ.put(message);
						assert(responseBQ.size() <= 2); // see documentation of responseBQ
					} catch (InterruptedException e) {
						throw new RuntimeException("InterruptedException");
					}
					return;
				}
				
				Utils.DEBUG_LOG_LINE("+++ Adding to regular queue of: " + Utils.showable(address) + ", payload.length():" + payload.length() );
				
				try {
					primitiveMessagesToHandle.put(payload);
				} catch (Exception e) {
					throw new RuntimeException("failded to put in primitiveMessagesToHandle");
				}

		});
		
		listenThread = new Thread(() -> {
				listeningLoopOnDedicatedThread();
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
		
		// To let deliveries under process to finish succesfully.
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
				Utils.DEBUG_LOG_LINE("Took, _address=" + Utils.showable(address) + ", msg=" + response); 
				return response.payload;
			}
			try {
				responseBQ.put(response);
				Thread.sleep(50); // give the other thread a chance to grab the response.
			} catch (InterruptedException e) {
				throw new RuntimeException("InterruptedException");
			} 
		}
		
	}

	
	/*
	 * Only a single non-empty message can be consumed at any given time.
	 * This function is run on the listen-loop thread.
	 */
	private void newMessageArrivedCallback(String data)
	{
		
		Utils.DEBUG_LOG_LINE("NewArrived. _address="+ Utils.showable(address) + ", data.length()="
		+ data.length() + ", data=" + Utils.showable(data));
		
		assert (!data.isEmpty());
		
		PayLoad message = getInnerMessageFromPayload(data);
		
		assert(message.responseTargetId == null);
		  
		Utils.DEBUG_LOG_LINE("---regular-consume: " + message);

		
		assert(currentMessageConsumedId == null);
		currentMessageConsumedId = message.messageId;
		consumer.accept(message.sender, message.payload);
		currentMessageConsumedId = null;
			
	}

	
	/**
	 * Sends a 'data' string to 'targetAddress', without a chance to fail.
	 * This function runs either on the user's thread or on the listen loop thread.
	 * @param respnseTargetId Should be null if 'data' is not a response.
	 * @param newMessageId If null, nextMessageIdToGive is used and incremented.
	 * @throws MessengerException 
	 */
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
		int TMP__tries = 0;
		synchronized (sendingLock)
		{
			assert(!messageRecivedIndicator);
			while (!messageRecivedIndicator)
			{
				TMP__tries++;
				String payload = Utils.fromObjectToGsonStr(newMessage);
				
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
		return Utils.fromGsonStrToObject(payload, PayLoad.class);
	}

}


