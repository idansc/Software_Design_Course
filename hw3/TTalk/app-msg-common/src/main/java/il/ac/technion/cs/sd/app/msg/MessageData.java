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
	TaskType _taskType;
	
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
	MessageData(TaskType taskType){
		_taskType = taskType;

	}
	MessageData(TaskType taskType,String target){
		_taskType = taskType;
		_target = target;
	}
	MessageData(TaskType taskType, InstantMessage message){
		_taskType = taskType;
		_message = message;
	}

}
