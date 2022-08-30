package org.fiware.apollo.exception;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;

@Produces
@Singleton
@Requires(classes = {UpdateFailureException.class, ExceptionHandler.class})
@Slf4j
public class CreationFailureExceptionHandler implements ExceptionHandler<CreationFailureException, HttpResponse<?>> {

	@Override
	public HttpResponse<?> handle(HttpRequest request, CreationFailureException exception) {
		log.error("Was not able to create entity.", exception);
		return HttpResponse.status(HttpStatus.BAD_GATEWAY).body("Was not able to create entity.");
	}
}
