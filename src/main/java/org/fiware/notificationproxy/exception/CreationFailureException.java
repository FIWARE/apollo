package org.fiware.notificationproxy.exception;

public class CreationFailureException extends Exception {
	public CreationFailureException(String message) {
		super(message);
	}

	public CreationFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
