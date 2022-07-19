package org.fiware.apollo.exception;

/**
 * Exception to be thrown in case the entity could not have been created.
 */
public class CreationFailureException extends Exception {
	public CreationFailureException(String message) {
		super(message);
	}

	public CreationFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
