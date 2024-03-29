package org.fiware.apollo.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.reactivex.Single;
import org.fiware.apollo.exception.CreationFailureException;
import org.fiware.apollo.exception.NoSuchEntityException;
import org.fiware.apollo.exception.UpdateFailureException;
import org.fiware.apollo.mapping.EntityMapper;
import org.fiware.apollo.mapping.EntityMapperImpl;
import org.fiware.apollo.model.NotificationVO;
import org.fiware.apollo.model.NotifiedEntityVO;
import org.fiware.apollo.repository.EntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotificationControllerTest {

	private EntityRepository entityRepository;
	private EntityMapper entityMapper;
	private NotificationController notificationController;

	@BeforeEach
	public void setup() {
		entityMapper = new EntityMapperImpl();
		entityRepository = mock(EntityRepository.class);
		notificationController = new NotificationController(entityMapper, entityRepository);
	}

	@ParameterizedTest
	@MethodSource("notificationStream")
	public void receiveNotification_badRequest_create(NotificationVO testNotification) throws Exception {

		when(entityRepository.updateEntity(any())).thenReturn(Single.just(UpdateResult.NOT_FOUND));
		when(entityRepository.createEntity(any())).thenReturn(Single.just(false));

		HttpResponse<Object> response = notificationController.receiveNotification(testNotification).blockingGet();
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A bad request should be returned if nothing can be created.");
	}

	@ParameterizedTest
	@MethodSource("notificationStream")
	public void receiveNotification_badRequest_update(NotificationVO testNotification) throws Exception {

		when(entityRepository.updateEntity(any())).thenReturn(Single.just(UpdateResult.ERROR));

		HttpResponse<Object> response = notificationController.receiveNotification(testNotification).blockingGet();
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A bad request should be returned if nothing can be created.");
	}

	@Test
	public void receiveNotification_multiStatus_update() throws Exception {
		NotificationVO testNotification = new NotificationVO()
				.id(URI.create("urn:ngsi-ld:notification:test"))
				.type(NotificationVO.Type.NOTIFICATION)
				.data(List.of(
						new NotifiedEntityVO()
								.id(URI.create("urn:ngsi-ld:entity:test")),
						new NotifiedEntityVO()
								.id(URI.create("urn:ngsi-ld:entity2:test")),
						new NotifiedEntityVO()
								.id(URI.create("urn:ngsi-ld:entity3:test"))
								.setAdditionalProperties(Map.of("temp", Map.of("type", "Property", "value", 38)))));


		doReturn(Single.just(UpdateResult.ERROR)).doReturn(Single.just(UpdateResult.UPDATED)).when(entityRepository).updateEntity(any());

		HttpResponse<Object> response = notificationController.receiveNotification(testNotification).blockingGet();
		assertEquals(HttpStatus.MULTI_STATUS, response.getStatus(), "Multi-Status should be returned if only some can be updated.");
	}

	@Test
	public void receiveNotification_multiStatus_create() throws Exception {
		NotificationVO testNotification = new NotificationVO()
				.id(URI.create("urn:ngsi-ld:notification:test"))
				.type(NotificationVO.Type.NOTIFICATION)
				.data(List.of(
						new NotifiedEntityVO()
								.id(URI.create("urn:ngsi-ld:entity:test")),
						new NotifiedEntityVO()
								.id(URI.create("urn:ngsi-ld:entity2:test")),
						new NotifiedEntityVO()
								.id(URI.create("urn:ngsi-ld:entity3:test"))
								.setAdditionalProperties(Map.of("temp", Map.of("type", "Property", "value", 38)))));

		when(entityRepository.updateEntity(any())).thenReturn(Single.just(UpdateResult.NOT_FOUND));
		doReturn(Single.just(false)).doReturn(Single.just(true)).when(entityRepository).createEntity(any());

		HttpResponse<Object> response = notificationController.receiveNotification(testNotification).blockingGet();
		assertEquals(HttpStatus.MULTI_STATUS, response.getStatus(), "Multi-Status should be returned if only some can be created.");
	}

	private static Stream<Arguments> notificationStream() {
		return Stream.of(
				Arguments.of(
						new NotificationVO()
								.id(URI.create("urn:ngsi-ld:notification:test"))
								.type(NotificationVO.Type.NOTIFICATION)
								.addDataItem(new NotifiedEntityVO().id(URI.create("urn:ngsi-ld:entity:test")))),
				Arguments.of(
						new NotificationVO()
								.id(URI.create("urn:ngsi-ld:notification:test"))
								.type(NotificationVO.Type.NOTIFICATION)
								.data(List.of(
										new NotifiedEntityVO()
												.id(URI.create("urn:ngsi-ld:entity:test")),
										new NotifiedEntityVO()
												.id(URI.create("urn:ngsi-ld:entity2:test")),
										new NotifiedEntityVO()
												.id(URI.create("urn:ngsi-ld:entity3:test"))
												.setAdditionalProperties(Map.of("temp", Map.of("type", "Property", "value", 38)))))
				),
				Arguments.of(new NotificationVO()
						.id(URI.create("urn:ngsi-ld:notification:test"))
						.type(NotificationVO.Type.NOTIFICATION)
						.data(List.of()))
		);
	}

}