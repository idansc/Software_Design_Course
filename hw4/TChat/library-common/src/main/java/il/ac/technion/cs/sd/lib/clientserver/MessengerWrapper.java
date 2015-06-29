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
	private BlockingQueue<InnerMessage> responseBQ = new LinkedBlockingQueue<InnerMessage>();
	Long currentMessageConsumedId;	
	Object consumptionLock = new Object();
	Long nextMessageIdToGive = 0L;	
	private static final int MAX_TIME_FOR_SUCCESFUL_DELIVERY = 100; 
	private boolean messageRecivedIndicator = false;
	private boolean waitingForRecepientConfirmation = false; // for debuging.
	Object sendingLock = new Object();
	
	
	boolean messageLoopRequestedToStop = false;
	boolean messageLoopCurrentlyRunning = false;
	BlockingQueue<String> primitiveMessagesToHandle = new LinkedBlockingQueue<>();
	Thread listenThread;

	private class InnerMessage
	{
		@SuppressWarnings("unused")
		InnerMessage() {}
		InnerMessage(long messageId, Long respnseTargetId, String data, String fromAddress) {
			this.messageId = messageId;
			this.responseTargetId = respnseTargetId;
			this.data = data;
			this.fromAddress = fromAddress;
		}

		Long messageId;
		Long responseTargetId; 
		
		String data;
		
		String fromAddress;

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
					assert(waitingForRecepientConfirmation);
					messageRecivedIndicator = true;
					
					return;
				} 
				
				sendRecipeintConfirmation(payload);
				
				InnerMessage message = getInnerMessageFromPayload(payload);
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
				listeningLoop();
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
		
		InnerMessage response;
		
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
				return response.data;
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
		
		InnerMessage message = getInnerMessageFromPayload(data);
		
		assert(message.responseTargetId == null);
		  
		Utils.DEBUG_LOG_LINE("---regular-consume: " + message);

		
		assert(currentMessageConsumedId == null);
		currentMessageConsumedId = message.messageId;
		consumer.accept(message.fromAddress, message.data);
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
		InnerMessage newMessage = new InnerMessage(newMessageId,respnseTargetId, data, address);
		
		int TMP__tries = 0;
		
		synchronized (sendingLock)
		{
			
			assert(!messageRecivedIndicator);
			assert(!waitingForRecepientConfirmation);
			
			
			Utils.DEBUG_LOG_LINE(">>>Sending message from " + address + ", msg=" + newMessage); 
			
			
			/*
			 * The synchronization is important, otherwise, we won't know for which message the 
			 * "ok, I've got it" empty message refers to. 
			 */
			while (!messageRecivedIndicator)
			{
				
				TMP__tries++;
				Utils.DEBUG_LOG_LINE("===try #" + TMP__tries + "     (by " + address + ")");
				
				
				String payload = Utils.fromObjectToGsonStr(newMessage);
				
				waitingForRecepientConfirmation = true;
				primitiveSendRepeatedly(targetAddress, payload);
				try {
					Thread.sleep(MAX_TIME_FOR_SUCCESFUL_DELIVERY);
				} catch (InterruptedException e) {
					throw new RuntimeException("InterruptedException");
				}
			}
			waitingForRecepientConfirmation = false;
			
			Utils.DEBUG_LOG_LINE("===success   (by " + address + ")"); 
			
			messageRecivedIndicator = false;
		}
		
		
	}


	/* sends a message via _messenger, repeatedly, until recipient is a valid messenger (note that 
	 * it can still fail to be delivered).
	 */
	private void primitiveSendRepeatedly(String to, String payload)
	{
		assert(to != null && payload != null);	
		while (true)
		{	
			try {
				if (payload.isEmpty())
				{
					Utils.DEBUG_LOG_LINE("|||||| Sending empty message, from: " + me.getAddress() + "; to: " + to + " |||||||||");
				}
				me.send(to, payload);
				return;
			} catch (MessengerException e) {
				//trying again///
				try {
					Thread.sleep(10);
				} catch (InterruptedException e1) {
					assert(false);
				}
			}
		}
	}

	
	/* Runs on a single dedicated thread */
	private void listeningLoop()
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
			} else
			{
				Utils.DEBUG_LOG_LINE("____________________queue empty for: " + address); 
			}
		}
		messageLoopRequestedToStop = false;
		messageLoopCurrentlyRunning = false;
		
		Utils.DEBUG_LOG_LINE("!!!!!!!!!!!!!!!!!! LISTEN LOOP ENDED FOR: " + address);
	}
	
	/**
	 * sends a confirmation (empty message) that the payload was received.
	 * @param payload - a primitive payload received by _messenger.
	 */
	private void sendRecipeintConfirmation(String payload)
	{
		InnerMessage m = getInnerMessageFromPayload(payload);
		primitiveSendRepeatedly(m.fromAddress, "");
	}


	private InnerMessage getInnerMessageFromPayload(String payload) {
		return Utils.fromGsonStrToObject(payload, InnerMessage.class);
	}

}


