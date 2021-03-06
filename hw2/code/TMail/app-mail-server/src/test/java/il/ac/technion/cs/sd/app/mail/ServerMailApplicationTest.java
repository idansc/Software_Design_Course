package il.ac.technion.cs.sd.app.mail;

import il.ac.technion.cs.sd.lib.clientserver.Server;
import il.ac.technion.cs.sd.lib.clientserver.Server.ListenLoopAlreadyBeingDone;
import il.ac.technion.cs.sd.lib.clientserver.Server.NoCurrentListenLoop;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;



/*************************************************************
 * *************************************************************
 * *************************************************************
 * Note:
 * This unit test uses Mockito to test basic behavior of 
 * ServerMailApplication, independently from the client.
 * The "TMailIntegrationTest" under app-mail-tests tests the logic of
 * the app extensively.
 * *************************************************************
 * *************************************************************
 *************************************************************/



public class ServerMailApplicationTest {

	private InputStream inBufferStream = null;
	private OutputStream outBufferStream = null;
	ServerMailApplication serverMail;
	
	Server serverMock;
	
	
	// Use buffers instead of disk.
	private class PersistentConfigWithBuffers implements PersistentConfig {
		@Override
		public InputStream getPersistentMailInputStream() throws IOException {
			return inBufferStream;
		}
		
		@Override
		public OutputStream getPersistentMailOverwriteOutputStream() throws FileNotFoundException {
			return outBufferStream;
		}
	}
	
	
	@Before
	public void setUp() throws Exception {
		
		serverMail = new ServerMailApplication("server1");
		serverMail.setPersistentConfig(new PersistentConfigWithBuffers());
		
		serverMock = Mockito.mock(Server.class);
		
		
	} 


	
	@Test ()
	public void starListen() throws InterruptedException {
		serverMail.setServer(serverMock);
		
		serverMail.start();
		
		Mockito.verify(serverMock,Mockito.only()).startListenLoop(Mockito.any());
	}
	
	@Test (expected=ListenLoopAlreadyBeingDone.class)
	public void starWhenAlreadyStarted() {
		serverMail.start();
		serverMail.start();
	}
	
	
	@Test (expected=NoCurrentListenLoop.class)
	public void stopWhenAlteadyStopped() {
		serverMail.stop();
	}

}
