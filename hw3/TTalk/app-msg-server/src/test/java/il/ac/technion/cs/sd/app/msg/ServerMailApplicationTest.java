/**
 * 
 */
package il.ac.technion.cs.sd.app.msg;

import static org.junit.Assert.*;
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


	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	
	}

	@Test
	public void verifyStartListenLoop() {
		mockServer = Mockito.mock(Server.class);
		appServer.setServer(mockServer);
		appServer.start();
		Mockito.verify(mockServer,Mockito.atLeastOnce()).startListenLoop(Mockito.any(), Mockito.any());
	}

}
