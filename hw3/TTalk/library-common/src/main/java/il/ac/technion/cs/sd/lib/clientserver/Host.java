package il.ac.technion.cs.sd.lib.clientserver;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

/**
 * Represents a host (either client or server).
 * Provides communication functionality common to all hosts for the implementation of the client-server package.
 */
class Host {

	private Messenger _messenger;
	
	/**
	 * 
	 * @param address The address of the new host.
	 * @throws MessengerException 
	 */
	Host(String address) throws MessengerException
	{
		_messenger = new MessengerFactory().start(address, (String data) -> 
		{
			newMessageArrivedCallback(data);
		});
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
	void powerSend(String  targetAddress, String data) throws MessengerException, InterruptedException
	{
		assert(!messageRecivedIndicator);
		
		while (!messageRecivedIndicator)
		{
			_messenger.send(targetAddress, data);
			Thread.sleep(MAX_TIME_FOR_SUCCESFUL_DELIVERY);
		}
		
		messageRecivedIndicator = false;
	}
	
	private void newMessageArrivedCallback(String data)
	{
		
	}

	
}