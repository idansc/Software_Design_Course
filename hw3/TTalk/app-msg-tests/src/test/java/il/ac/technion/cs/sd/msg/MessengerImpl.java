package il.ac.technion.cs.sd.msg;

import java.io.UnsupportedEncodingException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

// The implementation of the 2.30, but with an added counter
// It's probably best to absolutely ignore class, instead of wasting time
// trying to understand it
public class MessengerImpl implements MessengerAux, Messenger {
	private static final Random					RANDOM	= new Random();
	private final String						address;
	private final BiConsumer<Messenger, String>	consumer;
	private boolean								stopped	= false;
	private final static AtomicInteger	i	= new AtomicInteger(0);
	public MessengerImpl(String address, BiConsumer<Messenger, String> consumer) {
		if (consumer == null)
			throw new IllegalArgumentException("Consumer cannot be null");
		this.consumer = consumer;
		if (address.matches("\\s*"))
			throw new IllegalArgumentException("Address cannot be whitespace");
		this.address = address;
	}
	
	private final BlockingQueue<String>	q			= new LinkedBlockingQueue<>();
	private Thread						thread;
	private String						waitingMessage;
	private Object						waitingLock	= new Object();
	private boolean						isWaiting	= false;
	
	private String getRmiAddress() {
		return getRmiAddresOf(this.address + "");
	}
	
	private static String getRmiAddresOf(String s) {
		return "localhost/" + s;
	}
	
	static {
		try {
			LocateRegistry.createRegistry(1099);
		} catch (RemoteException e) {
			if (e.getMessage()
					.replaceAll("\\s", "")
					.contains(
							"Port already in use: 1099; nested exception is: java.net.BindException: Address already in use"
									.replaceAll("\\s", ""))) {
				try {
					LocateRegistry.getRegistry(1099);
				} catch (RemoteException e1) {
					throw new AssertionError(e);
				}
			} else
				throw new AssertionError(e);
		}
	}
	
	public static MessengerImpl createInstance(String address,
			BiConsumer<Messenger, String> consumer) throws MessengerException {
		try {
			if (address == null)
				throw new IllegalArgumentException("Address cannot be null");
			MessengerImpl $ = new MessengerImpl(address, consumer);
			$.thread = new Thread(() -> $.listenLoop(), "MessengerListener@" + address);
			$.thread.setDaemon(true);
			$.thread.start();
			Thread.sleep(1);
			Thread.yield(); // should hopefully start the thread
			Remote stub = UnicastRemoteObject.exportObject($, 0);
			LocateRegistry.getRegistry().bind($.getRmiAddress(), stub);
			return $;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new MessengerException(e);
		}
	}
	
	 public synchronized void kill() throws MessengerException {
		try {
			stopped = true;
			if (thread != null)
				thread.interrupt();
			LocateRegistry.getRegistry().unbind(getRmiAddress());
			UnicastRemoteObject.unexportObject(this, true);
		} catch (Exception e) {
			throw new MessengerException(e);
		}
	}
	
	 public synchronized void send(String address, String payload)
			throws MessengerException {
		if (address == null)
			throw new IllegalArgumentException("Address cannot be null");
		if (payload == null)
			throw new IllegalArgumentException("Payload cannot be null");
		try {
			if (new String(payload.getBytes("US-ASCII")).equals(payload) == false)
				throw new MessengerException(
						"String is not a valid ASCII string");
			if (payload.length() > 0 && RANDOM.nextDouble() > 0.9)
				return;
		} catch (UnsupportedEncodingException e1) {
			throw new AssertionError(e1);
		}
		try {
			((MessengerAux)(LocateRegistry.getRegistry()
					.lookup(getRmiAddresOf(address)))).auxSend(payload);
			if (payload.length() > 0)
				i.incrementAndGet(); // only count empty messages
		} catch (Exception e) {
			throw new MessengerException(e);
		}
	}
	
	 public void auxSend(String payload) throws UnsupportedEncodingException,
			InterruptedException {
		synchronized (waitingLock) {
			if (!this.isWaiting) {
				q.add(payload);
				return;
			}
			this.waitingMessage = payload;
			waitingLock.notify();
			waitingLock.wait(); // saves you from races
		}
	}
	
	 public String getAddress() {
		return address + "";
	}
	
	private void listenLoop() {
		while (!stopped) {
			if (Thread.interrupted())
				return;
			try {
				String msg = q.poll(50, TimeUnit.MILLISECONDS);
				if (msg == null)
					continue;
				synchronized (this) {
					consumer.accept(this, msg);
				}
			} catch (InterruptedException e) {
				; // do nothing
			}
		}
	}
	
	 public synchronized void waitForMessage() {
		try {
			this.wait();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	 public String getNextMessage(long timeoutInMillis)
			throws InterruptedException {
		synchronized (waitingLock) {
			this.isWaiting = true;
			waitingLock.wait(timeoutInMillis);
			String $ = waitingMessage;
			this.waitingMessage = null;
			this.isWaiting = false;
			waitingLock.notify(); // wakes the waiting auxSend
			return $;
		}
	}
	
	 public String toString() {
		return "MessengerImpl@" + address;
	}
	
	 public synchronized String getLastOrNextMessage(long timeoutInMillis) throws InterruptedException {
		synchronized (waitingLock) {
			if (q.isEmpty() == false) {
				String $ = q.poll();
				assert $ != null;
				return $;
			}
			this.isWaiting = true;
			waitingLock.wait(timeoutInMillis);
			String $ = waitingMessage;
			this.waitingMessage = null;
			this.isWaiting = false;
			waitingLock.notify(); // wakes the waiting auxSend
			return $;
		}
	}

	public static void reset() {
		i.set(0);
	}

	public static int numberOfMessagesSent() {
		return i.get();
	}
	
}

