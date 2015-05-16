/**
 * 
 */
package il.ac.technion.cs.sd.app.mail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

import il.ac.technion.cs.sd.lib.clientserver.MessageData;
import il.ac.technion.cs.sd.lib.clientserver.ServerTask;
import il.ac.technion.cs.sd.msg.Messenger;

/**
 * @author idansc
 *
 */
public class ServerTaskMail implements ServerTask {
	String _serverAddress;
	enum FileType {
		IN_FILE,
		OUT_FILE
	}
	/* (non-Javadoc)
	 * @see il.ac.technion.cs.sd.lib.clientserver.ServerTask#run(il.ac.technion.cs.sd.msg.Messenger, il.ac.technion.cs.sd.lib.clientserver.MessageData)
	 */
	@Override
	public void run(Messenger serverMessenger, MessageData data) {
		_serverAddress = serverMessenger.getAddress();
		switch (TaskType.valueOf(data.getMessageType())) {
		case SEND_MAIL_TASK:
			Iterator<String> it = data.getData().iterator();
			String from = data.getFromAddress();
			String whom = it.next();
			String what = it.next();
			try {
				Files.write(getFilePath(from,FileType.OUT_FILE),
						Arrays.asList(from,whom,what));
				Files.write(getFilePath(whom,FileType.IN_FILE),
						Arrays.asList(from,whom,what));
			} catch (IOException e) {
				throw new RuntimeException();
			}
			break;

		default:
			break;
		}

	}
	
	Path getFilePath(String userAddress, FileType type){
		return Paths.get(_serverAddress, userAddress,type.toString());
	}

}
