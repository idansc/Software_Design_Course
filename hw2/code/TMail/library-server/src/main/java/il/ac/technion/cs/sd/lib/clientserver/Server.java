package il.ac.technion.cs.sd.lib.clientserver;

import java.util.Optional;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

/* Represents a server that performs a given task whenever data is received from 
 * any client. */
public class Server {

	private final String _serverAddress;

	/*
	 * This is thread running the listening loop. It's null iff no listen loop
	 * is currently done, or stopListenLoop was just called and the listen loop
	 * is going to stop.
	 */
	private Thread listenThread = null;
	private Messenger _messenger = null;
	private boolean currentlyListening = false;

	private boolean stopListenRequested = false;

	public Server(String serverAddress) {
		_serverAddress = serverAddress;
	}

	/**
	 * @return the server address
	 */
	public String getserverAddress() {
		return _serverAddress;
	}

	/*
	 * Starts a "listen loop" in which the server repeatedly listens for data
	 * from clients, and for each data package sent - the given task is run.
	 * This is done until stopListenLoop is called. This function is
	 * non-blocking (everything is done on another thread). It blocks only 
	 * until listening begins.
	 * 
	 * @param task The server task to be performed each time data is sent to the
	 * server. Once the task run is complete, a JSON MessageData will be sent
	 * back to the client with the messageType of
	 * MessageData.serverTaskEndedPacketType. Note: data sent to the server must
	 * contain a valid fromAdress field.
	 * 
	 * @throws ListenLoopAlreadyBeingDone
	 */
	public void startListenLoop(ServerTask task) throws InterruptedException {
		if (listenThread != null) {
			throw new ListenLoopAlreadyBeingDone();
		}
		stopListenRequested = false;

		listenThread = new Thread(() -> {
			try {
				_messenger = createMessenger();
				currentlyListening = true;
				while (!stopListenRequested) {
					Optional<byte[]> data = _messenger.tryListen();
					if (data.isPresent()) {

						MessageData md = MessageData.deserialize(data.get());
						if (md.getFromAddress() == null) {
							throw new RuntimeException(
									"Invalid 'fromAddress' field");
							// TODO: no indication of failure is given.
						}

			
						MessageData answer = task.run(md);;
						answer.setFromAddress(_serverAddress);
						_messenger.send(md.getFromAddress(), answer.serialize());
					}

					// If we cared about not wasting CPU time:
					// Thread.sleep(10);
				}
				currentlyListening = false;
				_messenger.kill();
				_messenger = null;
			} catch (MessengerException exc) {
				/*
				 * TODO: no indication is passed on. that's not good. Should pass
				 * the exception to main thread via some dedicated Server field.
				 */
			}

		});

		listenThread.start();
		while (!currentlyListening)
		{
			Thread.sleep(1);
		}
	}

	/*
	 * Stops the "listen loop" started with startListenLoop, and blocks until
	 * the listen loop thread actually terminates.
	 * @throws NoCurrentListenLoop
	 */
	public void stopListenLoop() throws InterruptedException {
		if (listenThread == null) {
			throw new NoCurrentListenLoop();
		}
		stopListenRequested = true;
		listenThread = null;
		Thread.yield();
		while (_messenger != null)
		{
			Thread.sleep(1);
		}
	}

	private Messenger createMessenger() throws MessengerException {
		return (new MessengerFactory()).start(_serverAddress);
	}

	public class NoCurrentListenLoop extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	public class ListenLoopAlreadyBeingDone extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

}
