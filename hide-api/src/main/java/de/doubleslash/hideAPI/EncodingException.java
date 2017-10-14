package de.doubleslash.hideAPI;

public class EncodingException extends Exception {
	private static final long serialVersionUID = 1L;

	public EncodingException() {
	}

	public EncodingException(String message) {
		super(message);
	}

	public EncodingException(Throwable t) {
		super(t);
	}
}