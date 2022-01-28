package org.fiware.notificationproxy.exception;

public class NoSuchEntityException extends Exception {
	public NoSuchEntityException(String message) {
		super(message);
	}

	public NoSuchEntityException(String message, Throwable cause) {
		super(message, cause);
	}
}
