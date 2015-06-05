package il.ac.technion.cs.sd.lib.clientserver;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

/**
 * Represents a reliable host (either client or server) - that always sends/receives successfully.
 * Provides general communication functionality common to all hosts for the implementation of the 
 * client-server package.
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
	Integer responseRequestorId; 
	
	long nextMessageIdToGive = 0;
	
	/**
	 * We'll send objects of this class via Messenger.
	 */
	private class InnerMessage
	{
		InnerMessage() {}
		InnerMessage(long messageId, Long respnseTargetId, String data) {
			this.messageId = messageId;
			this.respnseTargetId = respnseTargetId;
			this.data = data;
		}

		long messageId;
		
		/* The id of the message that this message is the response to, 
		 * or null if this message is not a response. */
		Long respnseTargetId; 
		
		String data;
	}

	/**
	 * @param address The address of the new host.
	 * @throws MessengerException 
	 */
	@SuppressWarnings("unchecked")
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
	

	// length (character #) of the field holding the tag of the message.
	private final int TAG_FIELD_LENGHT = 20;
	
	
	/* the powerSend method "busy waits" on this field until its true, and then would resets it to
	 * false.
	 */
	private boolean messageRecivedIndicator = false;
	
	/**
	 * Sends a 'data' string to 'targetAddress', without a chance to fail.
	 * @throws MessengerException 
	 */
	void send(String  targetAddress, String data) throws MessengerException, InterruptedException
	{
TODO
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
		assert(BlockingQueue == null);
		responseBQ = new LinkedBlockingQueue();
		
		send(targetAddress, data);
		InnerMessage response = responseBQ.take();
		assert(responseBQ.isEmpty());
		responseBQ = null;
		
		assert(response.respnseTargetId == )
		return response.data;
	}
	
	 	/**
	 * Sends a 'data' string to 'targetAddress', without a chance to fail.
	 * @param respnseTargetId May be null.
	 * @return the id of the new message sent.
	 * @throws MessengerException 
	 */
	private long sendAndGetMessageId(String  targetAddress, String data, Long respnseTargetId) 
			throws MessengerException, InterruptedException
	{
		assert(!messageRecivedIndicator);
		
		InnerMessage newMessage = new InnerMessage(nextMessageIdToGive,respnseTargetId, data);
		long $ = nextMessageIdToGive++;
		
		while (!messageRecivedIndicator)
		{
			_messenger.send(targetAddress, data);
			Thread.sleep(MAX_TIME_FOR_SUCCESFUL_DELIVERY);
		}
		
		messageRecivedIndicator = false;
		
		return $;
	}

	
	private void newMessageArrivedCallback(String data)
	{
		if (data.isEmpty())
		{
			assert(!messageRecivedIndicator);
			messageRecivedIndicator = true;
			return;
		} 
		
		InnerMessage message = Utils.fromGsonStrToObject(data, Message.class);
		
		if (responseRequestorId != null && responseRequestorId == message.respnseTargetId)
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

}
