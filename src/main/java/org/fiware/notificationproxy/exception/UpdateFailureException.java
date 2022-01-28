package org.fiware.notificationproxy.exception;

public class UpdateFailureException extends Exception{
	public UpdateFailureException(String message) {
		super(message);
	}

	public UpdateFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
