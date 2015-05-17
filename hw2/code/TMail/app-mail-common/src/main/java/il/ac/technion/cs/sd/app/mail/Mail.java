package il.ac.technion.cs.sd.app.mail;

import java.util.ListIterator;

public class Mail {
	public final String from;
	public final String to;
	public final String content;

	
	// TODO: hide the following two in a child class of Mail that's private 
	//to the ServerTaskMail module.
	boolean alreadyRead = false;
	// An iterator to the element in allNewMailSentToPerson's value, representing
	// this object, or null (if non such exists).
	ListIterator<Mail> newMailIterator = null; 
	
	
	
	public Mail(String from, String to, String content) {
		super();
		this.from = from;
		this.to = to;
		this.content = content;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mail other = (Mail)obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}
	
	
}
