package il.ac.technion.cs.sd.lib.clientserver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
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

	private Messenger _messenger;
	
	private Consumer<String> _consumer;
	
	/* This is not null iff sendAndBlockUntilResponseArrives is currently waiting for a response. 
	 * A InnerMessage object representing the response will be pushed to this queue when received.
	 * This queue can hold maximum one element. */
	private BlockingQueue<InnerMessage> responseBQ;
	
	/* This is null iff responseBQ is null.
	 * When not null - this is the id of the message that requests a response. */
	Long responseRequestorId; 
	
	/*
	 * All modification to synchronized must be done while synchronized by the nextMessageIdToGive
	 * monitor. 
	 */
	Long nextMessageIdToGive = 0L;
	
	/* maximum time for a successful message to be delivered (from sending time to receiving time), 
	 * in milisec */
	private static final int MAX_TIME_FOR_SUCCESFUL_DELIVERY = 200;
	
	/**
	 * We'll send objects of this class via Messenger.
	 */
	private class InnerMessage
	{
		InnerMessage() {}
		InnerMessage(long messageId, Long respnseTargetId, String data) {
			this.messageId = messageId;
			this.responseTargetId = respnseTargetId;
			this.data = data;
		}

		Long messageId;
		
		/* The id of the message that this message is the response to, 
		 * or null if this message is not a response. */
		Long responseTargetId; 
		
		String data;
	}

	/**
	 * @param address The address of the new host.
	 * @throws MessengerException 
	 */
	ReliableHost(String address) throws MessengerException
	{
		_messenger = new MessengerFactory().start(address, (String data) -> 
		{
			newMessageArrivedCallback(data);
		});
	
		//consumerWithStr = () 
	}
	
	
	void startListenLoop(Consumer<String> consumer)
	{
		_consumer = consumer;
		
	}
	
	
	/* the powerSend method "busy waits" on this field until its true, and then would resets it to
	 * false.
	 */
	private boolean messageRecivedIndicator = false;
	
	/**
	 * Sends a 'data' string to 'targetAddress', without a chance to fail.
	 * Must not wait on 'this' monitor.
	 * @param respnseTargetId May be null.
	 * @throws MessengerException 
	 */
	void send(String targetAddress, String data, Long respnseTargetId) 
			throws MessengerException, InterruptedException
	{
		send(targetAddress, data, respnseTargetId, null);
	}
	
	
	/**
	 * Sends a message to the server, and blocks until a response message is received.
	 * @param targetAddress
	 * @param data
	 * @throws MessengerException
	 * @throws InterruptedException
	 */
	String sendAndBlockUntilResponseArrives(
			String  targetAddress, String data) throws MessengerException, InterruptedException
	{
		assert(responseBQ == null);
		assert(responseRequestorId == null);
		
		synchronized(nextMessageIdToGive)
		{
			responseRequestorId = nextMessageIdToGive;
			nextMessageIdToGive++;
		}
		responseBQ = new LinkedBlockingQueue<InnerMessage>();
		
		send(targetAddress, data, null, responseRequestorId);
		
		
		InnerMessage response = responseBQ.take();
		
		assert(responseBQ.isEmpty());
		responseBQ = null;
		
		assert(response.responseTargetId == responseRequestorId);
		responseRequestorId = null;
		
		return response.data;
	}


	
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

		_consumer.accept( message.data );
	}

	
	/**
	 * Sends a 'data' string to 'targetAddress', without a chance to fail.
	 * Must not wait on 'this' monitor.
	 * @param respnseTargetId May be null.
	 * @param newMessageId If null, nextMessageIdToGive is used and incremented.
	 * @throws MessengerException 
	 */
	private void send(String  targetAddress, String data, Long respnseTargetId, Long newMessageId) 
			throws MessengerException, InterruptedException
	{
		assert(!messageRecivedIndicator);
		
		if (newMessageId == null)
		{
			synchronized(nextMessageIdToGive)
			{
				newMessageId = nextMessageIdToGive;
				nextMessageIdToGive++;
			}
		}
		InnerMessage newMessage = new InnerMessage(newMessageId,respnseTargetId, data);
		
		while (!messageRecivedIndicator)
		{
			_messenger.send(targetAddress, data);
			Thread.sleep(MAX_TIME_FOR_SUCCESFUL_DELIVERY);
		}
		
		messageRecivedIndicator = false;
	}
	
}
