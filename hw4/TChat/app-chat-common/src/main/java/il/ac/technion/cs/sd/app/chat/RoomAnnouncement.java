package il.ac.technion.cs.sd.app.chat;

public class RoomAnnouncement {
	static enum Announcement {
		JOIN, LEAVE, DISCONNECT;
	}

	public final String who;
	public final String room;
	public final Announcement type;

	public RoomAnnouncement(String who, String room, Announcement type) {
		super();
		this.who = who;
		this.room = room;
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((room == null) ? 0 : room.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((who == null) ? 0 : who.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoomAnnouncement other = (RoomAnnouncement) obj;
		if (room == null) {
			if (other.room != null)
				return false;
		} else if (!room.equals(other.room))
			return false;
		if (type != other.type)
			return false;
		if (who == null) {
			if (other.who != null)
				return false;
		} else if (!who.equals(other.who))
			return false;
		return true;
	}
}
