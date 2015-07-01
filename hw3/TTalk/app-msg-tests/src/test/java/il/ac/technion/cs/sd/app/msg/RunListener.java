package il.ac.technion.cs.sd.app.msg;

/**
 * This class is used by maven to print a message whenever a test has started.
 */
public class RunListener extends org.junit.runner.notification.RunListener {

    public RunListener() {
        System.out.println("Creation of Run Listener...");
    }

    @Override
    public void testStarted(org.junit.runner.Description description) throws Exception {
			System.out.println("Starting " + description.getDisplayName());
    }
}
