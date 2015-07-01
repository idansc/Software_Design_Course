/**
 * 
 */
package il.ac.technion.cs.sd.app.msg;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import il.ac.technion.cs.sd.lib.clientserver.Server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author idansc
 *
 */
public class ServerMailApplicationTest {
	ServerMailApplication appServer;
	Server mockServer;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		appServer = new ServerMailApplication("T2");
		mockServer = Mockito.mock(Server.class);
		appServer.setServer(mockServer);
		Mockito.when(mockServer.<Map<String, ArrayList<MessageData>>>readObjectFromFile(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
		Mockito.when(mockServer.<Map<String, ArrayList<String>>>readObjectFromFile(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
		Mockito.when(mockServer.<Set<String>>readObjectFromFile(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
		Mockito.when(mockServer.readObjectFromFile(Mockito.any(), Mockito.any())).thenReturn(Optional.empty());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	
	}

	@Test
	public void verifyStartListenLoop() {
		appServer.setServer(mockServer);
		appServer.start();
		Mockito.verify(mockServer,Mockito.atLeastOnce()).start(Mockito.any(), Mockito.any());
	}
	
	@Test
	public void serverShouldSaveFiles() {
		appServer.stop();
		Mockito.verify(mockServer,Mockito.atLeastOnce()).saveObjectToFile(Mockito.anyString(), Mockito.any());
	}
	
	@Test
	public void serverShouldCleanFiles() {
		appServer.clean();
		Mockito.verify(mockServer,Mockito.only()).clearPersistentData();
	}

}