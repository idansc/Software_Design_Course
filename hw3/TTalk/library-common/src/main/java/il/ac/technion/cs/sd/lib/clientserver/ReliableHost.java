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
	 */
	Long currentMessageConsumedId;
	
	//This object is used as a monitor to synchronize messages consumptions.
	Object consumptionLock = new Object();
	
	/*
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
	
	Object sendingLock = new Object();
	
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
			return "[from:" + showable(fromAddress) + ",messageId=" + messageId + 
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
		
		System.out.println("Took, _address=" + showable(_address) + ", msg=" + response); //TODO:DELTE.
		
		
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
		
		//TODO: DELETE
		System.out.println("NewArrived. _address="+ showable(_address) + 
				", data.length()=" + data.length() + ", data=" + showable(data));
		
		if (data.isEmpty())
		{
			//TODO: DELETE
			System.out.println("\"\"   _address=" + showable(_address)); 
			
			assert(!messageRecivedIndicator);
			messageRecivedIndicator = true;
			return;
		} 
		
		InnerMessage message = Utils.fromGsonStrToObject(data, InnerMessage.class);
		
		System.out.println("msg=" + message);
		
		
		primitiveSendRepeatedly(message.fromAddress, "");
		
		
		
		if (responseRequestorId != null && responseRequestorId == message.responseTargetId)
		{
			
			//TODO: DELETE
			System.out.println("---responseBQ.put(message)");
			
			
			
			try {
				responseBQ.put(message);
			} catch (InterruptedException e) {
				throw new RuntimeException("InterruptedException");
			}
			return;
		}
		  
		//TODO: DELETE
		System.out.println("---regular-consume");

		
		synchronized(consumptionLock)
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
		
		int TMP__tries = 0; //TODO:DELETE
		
		synchronized (sendingLock)
		{
			
			
			System.out.println(">>>Sending message from " + _address + ", msg=" + newMessage); //TODO: DELETE
			
			
			/*
			 * The synchronization is important, otherwise, we won't know for which message the 
			 * "ok, I've got it" empty message refers to. 
			 */
			while (!messageRecivedIndicator)
			{
				
				//TODO:DELETE
				TMP__tries++;
				System.out.println("===try #" + TMP__tries + "     (by " + _address + ")");
				
				
				String payload = Utils.fromObjectToGsonStr(newMessage);
				primitiveSendRepeatedly(targetAddress, payload);
				try {
					Thread.sleep(MAX_TIME_FOR_SUCCESFUL_DELIVERY);
				} catch (InterruptedException e) {
					throw new RuntimeException("InterruptedException");
				}
			}
			
			System.out.println("===success   (by " + _address + ")"); //TODO:DELETE
			
		}
		
		messageRecivedIndicator = false;
	}


	/* sends a message via _messenger, repeatedly, until recipient is a valid messenger (note that 
	 * it can still fail to be delivered).
	 */
	private void primitiveSendRepeatedly(String to, String payload)
	{
		while (true)
		{	
			try {
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

	
	private String showable(String str)
	{
		if (str.length() <= 15)
			return str;
		return str.substring(0, 15);
	}
}


