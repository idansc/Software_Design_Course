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
	 * A Message object representing the response will be pushed to this queue when received.
	 * This queue can hold maximum one element. */
	private BlockingQueue<Message> responseBQ;
	
	/* This is null iff responseBQ is null.
	 * When not null - this is the id of the message that requests a response. */
	Integer responseRequestorId; 
	
	long nextMessageIdToGive = 0;
	
	/**
	 * We'll send objects of this class via Messenger.
	 */
	private class Message
	{
		Message() {}
		Message(int messageId, Integer respnseTargetId, String data) {
			this.messageId = messageId;
			this.respnseTargetId = respnseTargetId;
			this.data = data;
		}

		int messageId;
		
		/* The id of the message that this message is the response to, 
		 * or null if this message is not a response. */
		Integer respnseTargetId; 
		
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
		Message response = responseBQ.take();
		assert(responseBQ.isEmpty());
		responseBQ = null;
		
		assert(response.respnseTargetId == )
		return response.data;
	}
	
	 	/**
	 * Sends a 'data' string to 'targetAddress', without a chance to fail.
	 * @return the id of the new message sent.
	 * @throws MessengerException 
	 */
	private int sendAndGetMessageId(String  targetAddress, String data) 
			throws MessengerException, InterruptedException
	{
		assert(!messageRecivedIndicator);
		
		Message newMessage = new Message();
		nextMessageIdToGive++;
		
		while (!messageRecivedIndicator)
		{
			_messenger.send(targetAddress, data);
			Thread.sleep(MAX_TIME_FOR_SUCCESFUL_DELIVERY);
		}
		
		messageRecivedIndicator = false;
	}

	
	private void newMessageArrivedCallback(String data)
	{
		
		if (data.isEmpty())
		{
			assert(!messageRecivedIndicator);
			messageRecivedIndicator = true;
			return;
		} 
		
		Message message = Utils.fromGsonStrToObject(data, Message.class);
		_consumer.accept( message.data );
	}

}
