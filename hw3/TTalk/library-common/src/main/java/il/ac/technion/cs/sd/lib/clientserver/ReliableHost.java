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
	private Consumer<String> consumer = null;
	
	private String magicStrRepresentingEmptyStr = "zQSGRo9ODtGO60v0nUht";

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
	
	
	
	// max time for a successful delivery of a message (from sending until receiving) in milisec.
	private int MAX_TIME_FOR_SUCCESFUL_DELIVERY = 200;
	
	
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
		
		/*
		 * Actual empty string messages are represented by magicStrRepresentingEmptyStr.
		 */
		
		if (data.isEmpty())
		{
			data = magicStrRepresentingEmptyStr;
		}
		
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
		/* 
		 * Empty message from A to B is a signal for B: "I've recived the message you just sent me.
		 * Actual empty string messages are represented by magicStrRepresentingEmptyStr.
		 */
		
		if (data.isEmpty())
		{
			assert(!messageRecivedIndicator);
			messageRecivedIndicator = true;
		} else
		{
			if (data.equals(magicStrRepresentingEmptyStr))
			{
				data = "";
			}
			consumer.accept(data);
		}
	}
}
