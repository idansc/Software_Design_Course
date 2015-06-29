package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.After;
import org.junit.Test;

public class TestSend {
	private ServerLib s;
	private ClientLib c2=null;
	private ClientLib c1=null;
	
	@Test
	public void sendToServer() throws InterruptedException{
		BlockingQueue<String> mails = new LinkedBlockingQueue<>();
		c1=new ClientLib("c1", (x)->{}, "s");
		c2=new ClientLib("c2", (x)->{}, "s");
		s=new ServerLib("s", (req,s)->{mails.add(req);});
		s.start();
		Thread t=new Thread(()->{c2.blockingSendToServer("crap");});
		t.start();
		c1.blockingSend("s","crap");
		t.join();
		assertEquals("crap",mails.take());
		assertEquals("crap", mails.take());
	}
	
	@Test 
	public void sendFromConsumerShouldWork() throws InterruptedException{
		BlockingQueue<String> mails = new LinkedBlockingQueue<>();
		s=new ServerLib("s", (req,s)->{mails.add(req);});
		c1=new ClientLib("c1", (x)->{}, "s");
		c2=new ClientLib("c2", (x)->{
			c2.sendFromConsumer("got it");
			}, "s");
		s=new ServerLib("s", (req,s)->{mails.add(req);});
		s.start();
		c1.blockingSend("c2", "");
		assertEquals("got it", mails.take());
		
	}
	
	@Test
	public void sendRecieve() throws InterruptedException{
		BlockingQueue<String> mails = new LinkedBlockingQueue<>();
		s=new ServerLib("s", (req,s)->{if (req.equals("")){
			s.blockingRespond("c1", "yo mama joke");
			}
		else {
			mails.add(req);
		}
		});
		c1=new ClientLib("c1", (x)->{mails.add(x);}, "s");
		c2=new ClientLib("c2", (x)->{},"s");
		s.start();
		new Thread(()->{c2.blockingSendToServer("arg");}).start();
		assertEquals(c1.sendRecieve(""),"yo mama joke");
		assertEquals("arg", mails.take());
		assertEquals(0,mails.size());
	}
	
	@Test
	public void sendFromServer() throws InterruptedException{
		BlockingQueue<String> mails = new LinkedBlockingQueue<>();
		s=new ServerLib("s", (req,s)->{if (req.equals("")){
			s.blockingSend("c2", "yo mama joke");
			}
		else {
			mails.add(req);
		}
		});
		s.start();
		c1=new ClientLib("c1", (x)->{}, "s");
		c2=new ClientLib("c2", (x)->{c2.sendFromConsumer("yo");},"s");
		c1.blockingSendToServer("");
		assertEquals("yo", mails.take());
	}
	
	@After
	public void die(){
		s.kill();
		c1.kill();
		c2.kill();
	}
}
