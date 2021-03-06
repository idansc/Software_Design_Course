/**
 * 
 */
package il.ac.technion.cs.sd.app.msg;


/**
 * @author idansc
 *
 */
class MessageData{

	ServerTaskType _serverTaskType;
	
	InstantMessage _message;
	
	String _from;
	String _target;
	Boolean _friendRequestAnswer;
	

	MessageData(ServerTaskType taskType){
		_serverTaskType = taskType;
		
	}
	MessageData(ServerTaskType serverTaskType,Boolean friendRequestAnswer,String target){
		_serverTaskType = serverTaskType;
		_target = target;
		_friendRequestAnswer = friendRequestAnswer;

	}
	MessageData(ServerTaskType serverTaskType, InstantMessage message){
		_serverTaskType = serverTaskType;
		_message = message;
		
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
