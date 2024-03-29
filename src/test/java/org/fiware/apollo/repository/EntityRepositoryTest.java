package org.fiware.apollo.repository;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.reactivex.Single;
import org.fiware.apollo.rest.UpdateResult;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.apollo.configuration.GeneralProperties;
import org.fiware.apollo.exception.CreationFailureException;
import org.fiware.apollo.exception.UpdateFailureException;
import org.fiware.apollo.mapping.EntityMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
		when(entitiesApiClient.appendEntityAttrs(any(), any(), any(), any())).thenReturn(Single.just(HttpResponse.status(status)));
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test"));
		entityVO.setAdditionalProperties(Map.of("type", "Entity"));

		assertDoesNotThrow(() -> entityRepository.updateEntity(entityVO), "Nothing should happen.");
	}

	@ParameterizedTest
	@MethodSource("failureState")
	public void updateEntity_failure(HttpStatus status) throws Exception {
		when(entitiesApiClient.appendEntityAttrs(any(), any(), any(), any())).thenThrow(new HttpClientResponseException("Error", HttpResponse.status(status)));
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test"));
		entityVO.setAdditionalProperties(Map.of("type", "Entity"));
		UpdateResult updateResult = entityRepository.updateEntity(entityVO).blockingGet();
		if (status.equals(HttpStatus.NOT_FOUND)) {
			assertEquals(UpdateResult.NOT_FOUND, updateResult, "Not found should be signaled if the entity is not found.");
		} else {
			assertEquals(UpdateResult.ERROR, updateResult, "An update failure should be signaled if something unexpected happens.");
		}
	}

	@ParameterizedTest
	@MethodSource("okState")
	public void createEntity_success(HttpStatus status) throws Exception {
		when(entitiesApiClient.createEntity(any(), any())).thenReturn(Single.just(HttpResponse.status(status)));
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test"));
		entityVO.setAdditionalProperties(Map.of("type", "Entity"));
		assertDoesNotThrow(() -> entityRepository.createEntity(entityVO), "Nothing should happen.");
	}

	@ParameterizedTest
	@MethodSource("failureState")
	public void createEntity_failure(HttpStatus status) throws Exception {
		when(entitiesApiClient.createEntity(any(), any())).thenThrow(new HttpClientResponseException("Error", HttpResponse.status(status)));
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test"));
		entityVO.setAdditionalProperties(Map.of("type", "Entity"));
		assertFalse(entityRepository.createEntity(entityVO).blockingGet(), "An creation failure should be signaled if something unexpected happens.");
	}

	public static Stream<Arguments> okState() {
		return Arrays.stream(HttpStatus.values()).filter(s -> s.getCode() < 300).filter(s -> s.getCode() >= 200).map(Arguments::of);
	}

	public static Stream<Arguments> failureState() {
		return Stream.concat(Arrays.stream(HttpStatus.values()).filter(s -> s.getCode() > 300),
				Arrays.stream(HttpStatus.values()).filter(s -> s.getCode() < 200)).map(Arguments::of);
	}
}