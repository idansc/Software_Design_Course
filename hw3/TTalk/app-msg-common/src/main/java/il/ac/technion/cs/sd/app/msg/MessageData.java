/**
 * 
 */
package il.ac.technion.cs.sd.app.msg;


/**
 * @author idansc
 *
 */
class MessageData{
	ClientTaskType _clientTaskType;
	ServerTaskType _serverTaskType;
	
	InstantMessage _message;
	
	String _whom;
	String _target;
	Boolean _friendRequestAnswer;
	
	MessageData(ClientTaskType clientTaskType){
		_clientTaskType = clientTaskType;

	}
	MessageData(ClientTaskType clientTaskType, Boolean friendRequestAnswer){
		_clientTaskType = clientTaskType;
		_friendRequestAnswer = friendRequestAnswer;

	}
	MessageData(ClientTaskType clientTaskType, InstantMessage message){
		_clientTaskType = clientTaskType;
		_message = message;
		
	}
	MessageData(ServerTaskType taskType){
		_serverTaskType = taskType;

	}
	MessageData(ServerTaskType taskType,String target){
		_serverTaskType = taskType;
		_target = target;
	}
	MessageData(ServerTaskType taskType, InstantMessage message, String target){
		_serverTaskType = taskType;
		_message = message;
		_target = target;
	}

}
