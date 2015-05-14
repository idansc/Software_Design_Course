package il.ac.technion.cs.sd.msg;

public class RunThisFirst {

	public static void main(String[] args) throws Exception {
		Messenger m = new MessengerFactory().start("a");
		while (true) {
			System.out.println("Listening...");
			System.out.println(new String(m.listen()));
		}
	}

}
