package il.ac.technion.cs.sd.app.chat;

/**
 * Thrown when the user tries to get a list of clients in a room that doesn't exist
 */
public class NoSuchRoomException extends Exception {
	private static final long serialVersionUID = -6591714785116270419L;

	public NoSuchRoomException() {
		super();
	}

	public NoSuchRoomException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NoSuchRoomException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoSuchRoomException(String message) {
		super(message);
	}

	public NoSuchRoomException(Throwable cause) {
		super(cause);
	}

}
