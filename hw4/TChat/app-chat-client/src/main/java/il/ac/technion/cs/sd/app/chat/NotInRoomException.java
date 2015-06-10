package il.ac.technion.cs.sd.app.chat;

/**
 * Thrown when the user tries to a leave a room he isn't in, or when he
 * ries to send a message to a room he hasn't joined
 */
public class NotInRoomException extends Exception {
	private static final long serialVersionUID = 1221753555303502769L;

	public NotInRoomException() {
		super();
	}

	public NotInRoomException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NotInRoomException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotInRoomException(String message) {
		super(message);
	}

	public NotInRoomException(Throwable cause) {
		super(cause);
	}

}
