package il.ac.technion.cs.sd.lib.clientserver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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

	private boolean currentlyListening = false;
	private String _address;
	private Messenger _messenger;
	
	public String getAddress() {
		return _address;
	}

	// The first argument is the sender's address, the second is the data.
	private BiConsumer<String, String> _consumer;
	
	/* This is not null iff sendAndBlockUntilResponseArrives is currently waiting for a response. 
	 * A InnerMessage object representing the response will be pushed to this queue when received.
	 * This queue can hold maximum one element. */
	private BlockingQueue<InnerMessage> responseBQ;
	
	/* This is null iff responseBQ is null.
	 * When not null - this is the id of the message that requests a response. */
	Long responseRequestorId;
	
	/*
	 * This is not null iff a message is currently being consumed by '_consumer'.
	 * sendResponse uses this variable to determine the response target. 
	 * Note that this is an id of a message sent - so it may collide with an arbitrary message
	 * this object has sent (and it's no problem).
	 * This object is used as a monitor to synchronize messages consumptions.
	 */
	Long currentMessageConsumedId;
	
	/*
	 * All modification to synchronized must be done while synchronized by the nextMessageIdToGive
	 * monitor. 
	 */
	Long nextMessageIdToGive = 0L;
	
	/* maximum time for a successful message to be delivered (from sending time to receiving time), 
	 * in milisec */
	private static final int MAX_TIME_FOR_SUCCESFUL_DELIVERY = 200;
	
	/* the powerSend method "busy waits" on this field until its true, and then would resets it to
	 * false.
	 */
	private boolean messageRecivedIndicator = false;
	
	Object sendingLock;
	
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
		if (currentlyListening)
		{
			throw new InvalidOperation();
		}
		_consumer = consumer;
		
		_messenger = new MessengerFactory().start(_address, (String data) -> 
		{
			newMessageArrivedCallback(data);
		});
		
		currentlyListening = true;
	}
	
	void stop()
	{
		if (!currentlyListening)
		{
			throw new InvalidOperation();
		}
		
		// To let deliveries under process to finish succesfully.
		try {
			Thread.sleep(MAX_TIME_FOR_SUCCESFUL_DELIVERY);
		} catch (InterruptedException e1) {
			throw new RuntimeException("MAX_TIME_FOR_SUCCESFUL_DELIVERY");
		}
		
		currentlyListening = false;
		
		try {
			_messenger.kill();
		} catch (MessengerException e) {
			throw new RuntimeException("failed to kill messenger!");
		}
		
		responseBQ = null;
		responseRequestorId = null;
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
	 * @param targetAddress
	 * @param data
	 * @throws MessengerException
	 * @throws InterruptedException
	 */
	String sendAndBlockUntilResponseArrives(
			String  targetAddress, String data) throws MessengerException
	{
		if (!currentlyListening)
		{
			throw new InvalidOperation();
		}
		
		
		assert(responseBQ == null);
		assert(responseRequestorId == null);
		
		
		synchronized(nextMessageIdToGive)
		{
			responseRequestorId = nextMessageIdToGive;
			nextMessageIdToGive++;
		}
		responseBQ = new LinkedBlockingQueue<InnerMessage>();
		
		send(targetAddress, data, null, responseRequestorId);
		
		
		InnerMessage response;
		try {
			response = responseBQ.take();
		} catch (InterruptedException e) {
			throw new RuntimeException("InterruptedException");
		}
		
		assert(responseBQ.isEmpty());
		responseBQ = null;
		
		assert(response.responseTargetId == responseRequestorId);
		responseRequestorId = null;
		
		return response.data;
	}

	
	/*
	 * Only a single non-empty message can be consumed at any given time.
	 */
	private void newMessageArrivedCallback(String data)
	{
		if (data.isEmpty())
		{
			assert(!messageRecivedIndicator);
			messageRecivedIndicator = true;
			return;
		} 
		
		InnerMessage message = Utils.fromGsonStrToObject(data, InnerMessage.class);
		
		if (responseRequestorId != null && responseRequestorId == message.responseTargetId)
		{
			try {
				responseBQ.put(message);
			} catch (InterruptedException e) {
				throw new RuntimeException("InterruptedException");
			}
			return;
		}

		
		synchronized(currentMessageConsumedId)
		{
			assert(currentMessageConsumedId == null);
			currentMessageConsumedId = message.messageId;
			_consumer.accept(message.fromAddress, message.data);
			currentMessageConsumedId = null;
		}
	}

	
	/**
	 * Sends a 'data' string to 'targetAddress', without a chance to fail.
	 * @param respnseTargetId Should be null if 'data' is not a response.
	 * @param newMessageId If null, nextMessageIdToGive is used and incremented.
	 * @throws MessengerException 
	 */
	private void send(String  targetAddress, String data, Long respnseTargetId, Long newMessageId) 
			throws MessengerException
	{
		if (!currentlyListening)
		{
			throw new InvalidOperation();
		}
		
		assert(!messageRecivedIndicator);
		
		if (newMessageId == null)
		{
			synchronized(nextMessageIdToGive)
			{
				newMessageId = nextMessageIdToGive;
				nextMessageIdToGive++;
			}
		}
		InnerMessage newMessage = new InnerMessage(newMessageId,respnseTargetId, data, _address);
		
		synchronized (sendingLock)
		{
			/*
			 * The synchronization is important, otherwise, we won't know for which message the 
			 * "ok, I've got it" empty message refers to. 
			 */
			while (!messageRecivedIndicator)
			{
				_messenger.send(targetAddress, Utils.fromObjectToGsonStr(newMessage));
				try {
					Thread.sleep(MAX_TIME_FOR_SUCCESFUL_DELIVERY);
				} catch (InterruptedException e) {
					throw new RuntimeException("InterruptedException");
				}
			}
		}
		
		messageRecivedIndicator = false;
	}


	
}
