package il.ac.technion.cs.sd.lib;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MessengerWrapper {
	protected final Messenger me;
	protected final BlockingQueue<Boolean> ack; 
	public static final String ack_mssg = "";
	protected final BlockingQueue<String> responds;
	
	/**
	 * sending data from messengerWrapper always successful
	 * keep sending data until ack
	 * @param address
	 * @param onRecieve
	 */
	public MessengerWrapper(String address,final Consumer<String> onRecieve) {
		ack = new LinkedBlockingQueue<>();
		responds = new LinkedBlockingQueue<>();
		BiConsumer<Messenger,String> messengerConsumer = (myMessenger,json)->{
			if (json.equals(ack_mssg)){
				ack.add(true);
				return;
			}
			PayLoad tmp=PayLoad.fromString(json);
			try {
				myMessenger.send(tmp.sender,ack_mssg);
			} catch (MessengerException e) {
				throw new RuntimeException(e);
			}
			if (tmp.respond){
				responds.add(tmp.payload);
				return;
			}
			onRecieve.accept(tmp.payload);
		};
		try {
			me = new MessengerFactory().start(address, messengerConsumer);
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}	
	}
	/**
	 * send data always successful
	 * don't use from the onRecieve consumer
	 * @param to
	 * @param payload
	 */
	public synchronized void dedicatedBlockingSend(String to,String payload, boolean respond){
		// try to resend every 100 milliseconds until ack 
		try {
			do {
				me.send(to, new PayLoad(payload, me.getAddress(), respond).toJson());
			} while (ack.poll(100, TimeUnit.MILLISECONDS) == null);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * kill the messenger wrapper 
	 * don't use messenger wrapper after it have been killed
	 */
	//TODO:OFER
	public void kill(){
		try {
			me.kill();
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * send from inside a messenger consumer
	 * have a bigger overhead
	 * @param to
	 * @param payload
	 */
	public void dedicatedSendFromConsumer(String to,String payload){
		try {
			do {
				me.send(to, new PayLoad(payload, me.getAddress(), false).toJson());
			} while (notAck(me.getNextMessage(100)));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean notAck(String string){
		return !ack_mssg.equals(string);
	}
	
	public String getAddress() {
		return me.getAddress();
	}
}
