package il.ac.technion.cs.sd.lib.clientserver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

/** 
 * Represents a reliable host (either client or server) - that always sends/receives successfully.
 * Provides general communication functionality common to all hosts for the implementation of the 
 * client-server package.
 * 
 * This class is not thread-safe (meaning you must not access an object of this class from multiple 
 * threads simultaneously). 
 */

class ReliableHost {

	private String _address;
	private Messenger _messenger;
	
	public String getAddress() {
		return _address;
	}

	// The first argument is the sender's address, the second is the data.
	private BiConsumer<String, String> _consumer;
	
	/* A queue of InnerMessage objects representing the responses messages for messages sent by this host.
	 * In practice, this queue will hold up to 2 elements (one for the user's thread and one for the
	 * listening loop thread) */
	private BlockingQueue<InnerMessage> responseBQ = new LinkedBlockingQueue<InnerMessage>();
	
	
	/*
	 * This is not null iff a message is currently being consumed by '_consumer'.
	 * sendResponse uses this variable to determine the response target. 
	 * Note that this is an id of a message sent - so it may collide with an arbitrary message
	 * this object has sent (and it's no problem).
	 */
	Long currentMessageConsumedId;
	
	//This object is used as a monitor to synchronize messages consumptions.
	Object consumptionLock = new Object();
	
	
	/* The id to give the next outgoing message.
	 * All modification to synchronized must be done while synchronized by the nextMessageIdToGive
	 * monitor. 
	 */
	Long nextMessageIdToGive = 0L;
	
	/* maximum time for a successful message to be delivered (from sending time to receiving time), 
	 * in milisec */
	private static final int MAX_TIME_FOR_SUCCESFUL_DELIVERY = 100; 
	//TODO: Gal recommended MAX_TIME_FOR_SUCCESFUL_DELIVERY=100, but 4 seems to be enough.
	
	/* the powerSend method "busy waits" on this field until its true, and then would resets it to
	 * false.
	 */
	private boolean messageRecivedIndicator = false;
	private boolean waitingForRecepientConfirmation = false; // for debuging.
	
	Object sendingLock = new Object();
	
	
	boolean messageLoopRequestedToStop = false;
	boolean messageLoopCurrentlyRunning = false;
	// queue for non-empty messages.
	BlockingQueue<String> primitiveMessagesToHandle = new LinkedBlockingQueue<>();
	Thread listenThread;

	
	/**
	 * We'll send objects of this class via Messenger.
	 */
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
		
		/* The id of the message that this message is the response to, 
		 * or null if this message is not a response. */
		Long responseTargetId; 
		
		String data;
		
		// The address of the sender.
		String fromAddress;
		
		
		@Override
		public String toString()
		{
			return "[from:" + Utils.showable(fromAddress) + ",messageId=" + messageId + 
			"," + "responseTargetId=" + responseTargetId + "]"; 
		}

	}

	/**
	 * @param address The address of the new host.
	 * @throws MessengerException 
	 */
	ReliableHost(String address)
	{
		_address = address;
	}
	
	
	/**
	 * @param consumer The first argument taken is the sender's address, the second is the data.
	 */
	void start(BiConsumer<String, String> consumer) throws MessengerException
	{
		if (messageLoopCurrentlyRunning)
		{
			throw new InvalidOperation();
		}
		
		_consumer = consumer;
		
		_messenger = new MessengerFactory().start(_address, payload -> {

				
				if (payload.isEmpty())
				{
					//TODO: DELETE
					Utils.DEBUG_LOG_LINE("\"\"   _address=" + Utils.showable(_address)); 
					
					assert(!messageRecivedIndicator);
					assert(waitingForRecepientConfirmation);
					messageRecivedIndicator = true;
					
					return;
				} 
				
				sendRecipeintConfirmation(payload);
				
				InnerMessage message = getInnerMessageFromPayload(payload);
				if (message.responseTargetId != null)
				{
					//TODO: DELETE
					Utils.DEBUG_LOG_LINE("---responseBQ.put: " + message);
					
					try {
						responseBQ.put(message);
						assert(responseBQ.size() <= 2); // see documentation of responseBQ
					} catch (InterruptedException e) {
						throw new RuntimeException("InterruptedException");
					}
					return;
				}
				
				
				
				//TODO: DELETE
				Utils.DEBUG_LOG_LINE("+++ Adding to regular queue of: " + Utils.showable(_address) + ", payload.length():" + payload.length() );
				
				
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
			_messenger.kill();
		} catch (MessengerException e) {
			throw new RuntimeException("failed to kill messenger!");
		}
		
		responseBQ.clear();
		currentMessageConsumedId = null;
	}
	

	
	/**
	 * Sends a 'data' string to 'targetAddress', without a chance to fail.
	 * @param respnseTargetId Should be null if 'data' is not a response.
	 * @param isResponse true iff 'data' is a response to a message previously sent by another host.
	 * When true, you must call this method only from the consumer of the listen loop (i.e., from the 
	 * callback function invoked by the message to which the response is for).
	 * @throws MessengerException 
	 */
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
	
	
	
	/**
	 * Sends a message to the server, and blocks until a response message is received.
	 * This function runs either on the user's thread or on the listen loop thread.
	 * @param targetAddress
	 * @param data
	 * @throws MessengerException
	 * @throws InterruptedException
	 */
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
				Utils.DEBUG_LOG_LINE("Took, _address=" + Utils.showable(_address) + ", msg=" + response); //TODO:DELTE.
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
		
		//TODO: DELETE
		Utils.DEBUG_LOG_LINE("NewArrived. _address="+ Utils.showable(_address) + ", data.length()="
		+ data.length() + ", data=" + Utils.showable(data));
		
		assert (!data.isEmpty());
		
		InnerMessage message = getInnerMessageFromPayload(data);
		
		assert(message.responseTargetId == null);
		  
		//TODO: DELETE
		Utils.DEBUG_LOG_LINE("---regular-consume: " + message);

		
		assert(currentMessageConsumedId == null);
		currentMessageConsumedId = message.messageId;
		_consumer.accept(message.fromAddress, message.data);
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
		InnerMessage newMessage = new InnerMessage(newMessageId,respnseTargetId, data, _address);
		
		int TMP__tries = 0; //TODO:DELETE
		
		synchronized (sendingLock)
		{
			
			assert(!messageRecivedIndicator);
			assert(!waitingForRecepientConfirmation);
			
			
			Utils.DEBUG_LOG_LINE(">>>Sending message from " + _address + ", msg=" + newMessage); //TODO: DELETE
			
			
			/*
			 * The synchronization is important, otherwise, we won't know for which message the 
			 * "ok, I've got it" empty message refers to. 
			 */
			while (!messageRecivedIndicator)
			{
				
				//TODO:DELETE
				TMP__tries++;
				Utils.DEBUG_LOG_LINE("===try #" + TMP__tries + "     (by " + _address + ")");
				
				
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
			
			Utils.DEBUG_LOG_LINE("===success   (by " + _address + ")"); //TODO:DELETE
			
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
					Utils.DEBUG_LOG_LINE("|||||| Sending empty message, from: " + _messenger.getAddress() + "; to: " + to + " |||||||||");
				}
				_messenger.send(to, payload);
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
				//TODO: delete
				Utils.DEBUG_LOG_LINE("____________________queue empty for: " + _address); 
			}
		}
		messageLoopRequestedToStop = false;
		messageLoopCurrentlyRunning = false;
		
		//TODO
		Utils.DEBUG_LOG_LINE("!!!!!!!!!!!!!!!!!! LISTEN LOOP ENDED FOR: " + _address);
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


