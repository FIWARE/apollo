package org.fiware.apollo.exception;

public class CreationFailureException extends Exception {
	public CreationFailureException(String message) {
		super(message);
	}

	public CreationFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
