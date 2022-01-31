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

import java.net.URI;

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

	@Test
	public void updateEntity_noContent() throws Exception {
		when(entitiesApiClient.appendEntityAttrs(any(), any(), any(), any())).thenReturn(HttpResponse.noContent());
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test")).type("Entity");

		assertDoesNotThrow(() -> entityRepository.updateEntity(entityVO), "Nothing should happen.");
	}

	@Test
	public void updateEntity_multiStatus() throws Exception {
		when(entitiesApiClient.appendEntityAttrs(any(), any(), any(), any())).thenReturn(HttpResponse.status(HttpStatus.MULTI_STATUS));
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test")).type("Entity");

		assertDoesNotThrow(() -> entityRepository.updateEntity(entityVO), "Nothing should happen.");
	}

	@Test
	public void updateEntity_failure() throws Exception {
		when(entitiesApiClient.appendEntityAttrs(any(), any(), any(), any())).thenThrow(new HttpClientResponseException("Error", HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)));
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test")).type("Entity");
		assertThrows(UpdateFailureException.class, () -> entityRepository.updateEntity(entityVO), "An updatefailure should be signaled if something unexpected happens.");
	}

	@Test
	public void createEntity_failure() throws Exception {
		when(entitiesApiClient.createEntity(any(), any())).thenThrow(new HttpClientResponseException("Error", HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)));
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test")).type("Entity");
		assertThrows(CreationFailureException.class, () -> entityRepository.createEntity(entityVO), "An creation failure should be signaled if something unexpected happens.");
	}

	@Test
	public void createEntity_conflict() throws Exception {
		when(entitiesApiClient.createEntity(any(), any())).thenThrow(new HttpClientResponseException("Error", HttpResponse.status(HttpStatus.CONFLICT)));
		EntityVO entityVO = new EntityVO().id(URI.create("urn:ngis-ld:entity:test")).type("Entity");
		assertThrows(CreationFailureException.class, () -> entityRepository.createEntity(entityVO), "An creation failure should be signaled if the entity already exists.");
	}
}