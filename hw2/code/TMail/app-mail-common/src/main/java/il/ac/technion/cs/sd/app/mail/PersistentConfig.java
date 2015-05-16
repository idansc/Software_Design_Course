package il.ac.technion.cs.sd.app.mail;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/* Represents configurations related to I/O of persistent data. */
interface PersistentConfig {
	
	/* Returns a stream from which persistent mails should be read */
	InputStream getPersistentMailInputStream() throws FileNotFoundException;
	
	/* Returns a stream to which persistent mails should be written.
	 * Anything written with the returned stream overwrites existing persistent 
	 * mails that was written with an older stream. */
	OutputStream getPersistentMailOverwriteOutputStream() throws FileNotFoundException;
}
