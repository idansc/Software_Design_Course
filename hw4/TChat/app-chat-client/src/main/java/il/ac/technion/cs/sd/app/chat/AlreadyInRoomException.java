package il.ac.technion.cs.sd.app.chat;

/**
 * Thrown when a user attempts to join a room he's already in  
 */
public class AlreadyInRoomException extends Exception {
	private static final long serialVersionUID = -6296286234957210761L;

	public AlreadyInRoomException() {
		super();
	}

	public AlreadyInRoomException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AlreadyInRoomException(String message, Throwable cause) {
		super(message, cause);
	}

	public AlreadyInRoomException(String message) {
		super(message);
	}

	public AlreadyInRoomException(Throwable cause) {
		super(cause);
	}

}
