package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClientServerIntegratedTest {

	private Server server1;
	private Server server2;
	private Client client1;
	private Client client2;
	
	
	
	private class POJO1
	{
		public int i;
		public String str;
		POJO1() {}
		POJO1(int i, String str) {
			this.i = i;
			this.str = str;
		}
	}
	
	private class POJO2
	{
		public int i;
		public String str;
		POJO2(int i, String str, List<POJO1> pojos) {
			this.i = i;
			this.str = str;
			this.pojos = pojos;
		}
		public List<POJO1> pojos;
	}
	
	
	@Before
	public void setUp() throws Exception {
		server1 = new Server("server1");
		server2 = new Server("server2");
		client1 = new Client("client1");
		client2 = new Client("client2");
	}

	@After
	public void tearDown() throws Exception {
		server1.clearPersistentData();
		server2.clearPersistentData();
	}

	@Test
	public void saveAndThenLoadSimpleObject() {
		
		POJO1 pojo1 = new POJO1(1, "hi");
		server1.saveObjectToFile("pojo1", pojo1, true);
		server1.readObjectFromFile(filename, type, readFromStartOfFile)
		
		
	}

}
