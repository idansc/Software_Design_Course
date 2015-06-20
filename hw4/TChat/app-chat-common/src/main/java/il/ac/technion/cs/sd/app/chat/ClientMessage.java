/**
 * 
 */
package il.ac.technion.cs.sd.app.chat;

/**
 * @author idansc
 *
 */
class ClientMessage {
	TaskClientType type;

	ChatMessage message;
	RoomAnnouncement announcement;

	public ClientMessage(TaskClientType type) {
		super();
		this.type = type;
	}
	
	ChatMessage getMessage() {
		return message;
	}

	ClientMessage setMessage(ChatMessage message) {
		this.message = message;
		return this;
	}

	RoomAnnouncement getAnnouncement() {
		return announcement;
	}

	ClientMessage setAnnouncement(RoomAnnouncement announcement) {
		this.announcement = announcement;
		return this;
	}
}
