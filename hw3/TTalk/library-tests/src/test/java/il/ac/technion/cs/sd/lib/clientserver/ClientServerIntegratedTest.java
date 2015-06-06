package il.ac.technion.cs.sd.lib.clientserver;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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

	private POJO1 pojo1_a;
	private POJO1 pojo1_b;
	
	private POJO2 pojo2_a;
	
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
		
		pojo1_a = new POJO1(1, "hi");
		pojo1_b = new POJO1(2, "bye");
		
		List pojos = new LinkedList();
		pojos.add(pojo1_a);
		pojos.add(pojo1_b);
		
		pojo2_a = new POJO2(-19,"oh yea!",pojos);
		
		
		server1 = new Server("server1_"+UUID.randomUUID().toString());
		server2 = new Server("server2_"+UUID.randomUUID().toString());
		client1 = new Client("client1_"+UUID.randomUUID().toString());
		client2 = new Client("client2_"+UUID.randomUUID().toString());
		
		server1.clearPersistentData();
		server2.clearPersistentData();
		
		biConsumer1_bq = new LinkedBlockingQueue<>(); 
		consumer1_bq = new LinkedBlockingQueue<>();
	}

	@After
	public void tearDown() throws Exception {
		server1.clearPersistentData();
		server2.clearPersistentData();
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
		
		server1.saveObjectToFile("pojo1", pojo1_a);
		server1.saveObjectToFile("pojo2", pojo1_b);
		
		Optional<POJO1> $ = server1.readObjectFromFile("pojo1", POJO1.class);
		assertEquals($.get(), pojo1_a);
		
		$ = server1.readObjectFromFile("pojo2", POJO1.class);
		assertEquals($.get(), pojo1_b);
	}
	
	
	@Test
	public void saveAndThenLoadComplexObjects() {		

		
		server1.saveObjectToFile("c", pojo2_a);
		
		Optional<POJO2> $ = server1.readObjectFromFile("c", POJO2.class );
		//Optional<POJO2> $ = server1.readObjectFromFile("c", new POJO2().getClass() );
	
		assertEquals($.get(), pojo2_a);
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
		
		server1.saveObjectToFile("pojo1", pojo1_a);
		
		server1.clearPersistentData();
		
		Optional<POJO1> $ = server1.readObjectFromFile("pojo1", POJO1.class);
		assertFalse($.isPresent());
	}
	
	
	@Test//TODO(timeout=10000)
	public void clientSendsToServerMessage() throws InterruptedException {
		
		
		client1.startListenLoop(server1.getAddress(), consumer1, POJO1.class);
		server1.startListenLoop(biConsumer1, POJO1.class);

		for (int i=0; i<1; i++) //TODO
		{
			client1.send(pojo1_a);
			Pair<POJO1,String> $ = biConsumer1_bq.take();
			assertEquals($.first, pojo1_a);
			assertEquals($.second, client1.getAddress());
		}
		
		client1.stopListenLoop();
		server1.stop();
	}
	
	@Test(timeout=3000)
	public void serverSendsToClientMessage() throws InterruptedException {
		
		client1.startListenLoop(server1.getAddress(), consumer1, POJO1.class);
		server1.startListenLoop(biConsumer1, POJO1.class);

		for (int i=0; i<10; i++)
		{
			server1.send(client1.getAddress(), pojo1_a, false);
			POJO1 $ = consumer1_bq.take();
			assertEquals($, pojo1_a);
		}
		
		client1.stopListenLoop();
		server1.stop();
	}
	
	
	@Test //TODO(timeout=3000)
	public void serverSendsResponseBackToClient() throws InterruptedException {

//TODO
//		client1.startListenLoop(server1.getAddress(), consumer1, POJO1.class);
//		server1.startListenLoop((pojo, str) ->
//		{
//			assertEquals(str, client1.getAddress());//TODO
//			server1.send(client1.getAddress(), pojo1_b, true);
//		}, POJO1.class);
//
//		for (int i=0; i<1; i++)//TODO
//		{
//			
//			//TODO
////			POJO1 $ = client1.sendAndBlockUntilResponseArrives(pojo1_a, POJO1.class);
////			assertEquals($,pojo1_b);
//		}			
//		
//		client1.stopListenLoop();
//		server1.stop();
	}
	
	

	
	@Test
	public void TODO()
	{
//		System.out.println(server1.getAddress());
//		

		
//		Server s = new Server("aaaa");
//		s.startListenLoop(biConsumer1, POJO1.class);
		//s.stop();
	}
}


