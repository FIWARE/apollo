package org.fiware.notificationproxy.repository;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.notificationproxy.configuration.GeneralProperties;
import org.fiware.notificationproxy.exception.CreationFailureException;
import org.fiware.notificationproxy.exception.UpdateFailureException;
import org.fiware.notificationproxy.mapping.EntityMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EntityRepositoryTest {

	private EntityRepository entityRepository;
	private EntitiesApiClient entitiesApiClient;

	@BeforeEach
	public void setup() {
		entitiesApiClient = mock(EntitiesApiClient.class);
		entityRepository = new EntityRepository(new GeneralProperties(), entitiesApiClient, new EntityMapperImpl());
	}

	@ParameterizedTest
	@MethodSource("okState")
	public void updateEntity_success(HttpStatus status) throws Exception {
		when(entitiesApiClient.appendEntityAttrs(any(), any(), any(), any())).thenReturn(HttpResponse.status(status));
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test")).type("Entity");

		assertDoesNotThrow(() -> entityRepository.updateEntity(entityVO), "Nothing should happen.");
	}

	@ParameterizedTest
	@MethodSource("failureState")
	public void updateEntity_failure(HttpStatus status) throws Exception {
		when(entitiesApiClient.appendEntityAttrs(any(), any(), any(), any())).thenThrow(new HttpClientResponseException("Error", HttpResponse.status(status)));
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test")).type("Entity");
		assertThrows(UpdateFailureException.class, () -> entityRepository.updateEntity(entityVO), "An updatefailure should be signaled if something unexpected happens.");
	}

	@ParameterizedTest
	@MethodSource("okState")
	public void createEntity_success(HttpStatus status) throws Exception {
		when(entitiesApiClient.createEntity(any(), any())).thenReturn(HttpResponse.status(status));
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test")).type("Entity");
		assertDoesNotThrow(() -> entityRepository.createEntity(entityVO), "Nothing should happen.");
	}

	@ParameterizedTest
	@MethodSource("failureState")
	public void createEntity_failure(HttpStatus status) throws Exception {
		when(entitiesApiClient.createEntity(any(), any())).thenThrow(new HttpClientResponseException("Error", HttpResponse.status(status)));
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test")).type("Entity");
		assertThrows(CreationFailureException.class, () -> entityRepository.createEntity(entityVO), "An creation failure should be signaled if something unexpected happens.");
	}

	public static Stream<Arguments> okState() {
		return Arrays.stream(HttpStatus.values()).filter(s -> s.getCode() < 300).filter(s -> s.getCode() >= 200).map(Arguments::of);
	}

	public static Stream<Arguments> failureState() {
		return Stream.concat(Arrays.stream(HttpStatus.values()).filter(s -> s.getCode() > 300),
				Arrays.stream(HttpStatus.values()).filter(s -> s.getCode() < 200)).map(Arguments::of);
	}
}