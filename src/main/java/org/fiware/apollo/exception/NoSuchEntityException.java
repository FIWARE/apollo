package org.fiware.apollo.exception;

/**
 * Exception to be thrown in case the requested entity does not exist.
 */
public class NoSuchEntityException extends Exception {
	public NoSuchEntityException(String message) {
		super(message);
	}

	public NoSuchEntityException(String message, Throwable cause) {
		super(message, cause);
	}
}
