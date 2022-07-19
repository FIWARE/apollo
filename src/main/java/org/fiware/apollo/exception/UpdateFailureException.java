package org.fiware.apollo.exception;

/**
 * Exception to be thrown in case an entity could not have been updated.
 */
public class UpdateFailureException extends Exception{
	public UpdateFailureException(String message) {
		super(message);
	}

	public UpdateFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
