package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

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
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			POJO1 other = (POJO1) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (i != other.i)
				return false;
			if (str == null) {
				if (other.str != null)
					return false;
			} else if (!str.equals(other.str))
				return false;
			return true;
		}
		private ClientServerIntegratedTest getOuterType() {
			return ClientServerIntegratedTest.this;
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
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			POJO2 other = (POJO2) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (i != other.i)
				return false;
			if (pojos == null) {
				if (other.pojos != null)
					return false;
			} else if (!pojos.equals(other.pojos))
				return false;
			if (str == null) {
				if (other.str != null)
					return false;
			} else if (!str.equals(other.str))
				return false;
			return true;
		}
		public List<POJO1> pojos;
		private ClientServerIntegratedTest getOuterType() {
			return ClientServerIntegratedTest.this;
		}
	}
	
	
	@Before
	public void setUp() throws Exception {
		server1 = new Server("server1");
		server2 = new Server("server2");
		client1 = new Client("client1");
		client2 = new Client("client2");
		
		server1.clearPersistentData();
		server2.clearPersistentData();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void saveAndThenLoadSimpleObject() {
		
		POJO1 pojo1 = new POJO1(1, "hi");
		
		server1.saveObjectToFile("pojo1", pojo1);
		
		Optional<POJO1> $ = server1.readObjectFromFile("pojo1", POJO1.class);
		assertEquals($.get(), pojo1);
	}

}
