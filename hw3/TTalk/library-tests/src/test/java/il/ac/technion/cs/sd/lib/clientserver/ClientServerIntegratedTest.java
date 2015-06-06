package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
			if (i != other.i)
				return false;
			if (str == null) {
				if (other.str != null)
					return false;
			} else if (!str.equals(other.str))
				return false;
			return true;
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
	}

	private POJO1 pojo1;
	private POJO1 pojo2;
	
	private class  Pair<F,S>
	{
		Pair(F first, S second) {
			this.first = first;
			this.second = second;
		}
		F first;
		S second;
	}
	
	BlockingQueue<Pair<POJO1,String>> biConsumer1_bq;
	private BiConsumer<POJO1,String> biConsumer1 = (p,from) ->
	{
		biConsumer1_bq.add(new Pair<POJO1,String>(p,from));
	};
	
	BlockingQueue<POJO1> consumer1_bq;
	private Consumer<POJO1> consumer1 = p ->
	{
		consumer1_bq.add(p);
	};
	
	
	@Before
	public void setUp() throws Exception {
		
		pojo1 = new POJO1(1, "hi");
		pojo2 = new POJO1(2, "bye");
		
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
	
	@Test
	public void saveAndThenLoadTwoSimpleObjects() {
		
		server1.saveObjectToFile("pojo1", pojo1);
		server1.saveObjectToFile("pojo2", pojo2);
		
		Optional<POJO1> $ = server1.readObjectFromFile("pojo1", POJO1.class);
		assertEquals($.get(), pojo1);
		
		$ = server1.readObjectFromFile("pojo2", POJO1.class);
		assertEquals($.get(), pojo2);
	}
	
	
	@Test
	public void saveAndThenLoadComplexObjects() {		
		List pojos = new LinkedList();
		pojos.add(pojo1);
		pojos.add(pojo2);
		
		POJO2 complex = new POJO2(5,"aaa",pojos);
		
		
		server1.saveObjectToFile("c", complex);
		
		Optional<POJO2> $ = server1.readObjectFromFile("c", POJO2.class );
		//Optional<POJO2> $ = server1.readObjectFromFile("c", new POJO2().getClass() );
	
		assertEquals($.get(), complex);
	}

	@Test
	public void saveAndThenLoadFromNewServerWithSameName() {
		
		POJO1 pojo1 = new POJO1(1, "hi");
		
		server1.startListenLoop(biConsumer1, POJO1.class);
		server1.saveObjectToFile("pojo1", pojo1);
		server1.stop();
		
		
		Server s = new Server("server1");
		s.startListenLoop(biConsumer1, POJO1.class);
		Optional<POJO1> $ = s.readObjectFromFile("pojo1", POJO1.class);
		s.stop();

		assertEquals($.get(), pojo1);
	}
	
	
	@Test
	public void saveAndThenLoadAfterClear() {
		
		server1.saveObjectToFile("pojo1", pojo1);
		
		server1.clearPersistentData();
		
		Optional<POJO1> $ = server1.readObjectFromFile("pojo1", POJO1.class);
		assertFalse($.isPresent());
	}
	
	
	@Test(timeout=990000)//TODO
	public void clientSendsToServerMessage() throws InterruptedException {
		
		
		client1.startListenLoop("server1", consumer1, POJO1.class);
		server1.startListenLoop(biConsumer1, POJO1.class);

		client1.send(pojo1);
		Pair<POJO1,String> $ = biConsumer1_bq.take();
		assertEquals($.first, pojo1);
		assertEquals($.second, "client1");
		
		client1.stopListenLoop();
		server1.stop();
		
	}
}
