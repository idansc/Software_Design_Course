package il.ac.technion.cs.sd.lib.clientserver;

import java.util.Optional;
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
	
	
	/**
	 * We'll send objects of this class via Messenger.
	 */
	private class Message
	{
		int messageId;
		
		/* The id of the message that this message is the response to, or null if not relevant. */
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
	
	
	/* the powerSend method would busy wait on this field until its true (and then would re-set it to
	 * false.
	 */
	private boolean messageRecivedIndicator = false;
	
	/**
	 * Sends a 'data' string to 'targetAddress', without a chance to fail.
	 * The sending action can't fail.
	 * @throws MessengerException 
	 */
	void send(String  targetAddress, String data) throws MessengerException, InterruptedException
	{
		
		

		
		assert(!messageRecivedIndicator);
		
		while (!messageRecivedIndicator)
		{
			_messenger.send(targetAddress, data);
			Thread.sleep(MAX_TIME_FOR_SUCCESFUL_DELIVERY);
		}
		
		messageRecivedIndicator = false;
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
		send(targetAddress, data);
		//TODO
	}
	
	
	private void newMessageArrivedCallback(String data)
	{
		
		if (data.isEmpty())
		{
			assert(!messageRecivedIndicator);
			messageRecivedIndicator = true;
		} else
		{
			
			_consumer.accept(data);
		}
	}
	
	private int getMessageTag(String message)
	{
		String tmp = message.substring(0, TAG_FIELD_LENGHT);
		return Integer.parseInt(tmp);
		
	}
	
	private String getMessageWithoutTag(String message)
	{
		return message.substring(TAG_FIELD_LENGHT, message.length());
	}
	
	private String getMessageWithTag(String messageWithoutTag, int tag)
	{
		tag.toString().
		return 
	}
}
