package il.ac.technion.cs.sd.app.chat;

class ServerMessage {
	TaskServerType serverTaskType;
	
	ChatMessage chatMessage;
	
	String room;

	public ServerMessage(TaskServerType serverTaskType) {
		this.serverTaskType = serverTaskType;
	}

	TaskServerType getServerTaskType() {
		return serverTaskType;
	}

	void setServerTaskType(TaskServerType serverTaskType) {
		this.serverTaskType = serverTaskType;
	}

	ChatMessage getChatMessage() {
		return chatMessage;
	}

	ServerMessage setChatMessage(ChatMessage chatMessage) {
		this.chatMessage = chatMessage;
		return this;
	}

	String getRoom() {
		return room;
	}

	ServerMessage setRoom(String room) {
		this.room = room;
		return this;
	}
	

	
}
