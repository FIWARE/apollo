package org.fiware.notificationproxy.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import org.fiware.notificationproxy.exception.CreationFailureException;
import org.fiware.notificationproxy.exception.NoSuchEntityException;
import org.fiware.notificationproxy.exception.UpdateFailureException;
import org.fiware.notificationproxy.mapping.EntityMapper;
import org.fiware.notificationproxy.mapping.EntityMapperImpl;
import org.fiware.notificationproxy.model.NotificationVO;
import org.fiware.notificationproxy.model.NotifiedEntityVO;
import org.fiware.notificationproxy.model.PropertyVO;
import org.fiware.notificationproxy.repository.EntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
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

		doThrow(new NoSuchEntityException("404")).when(entityRepository).updateEntity(any());
		doThrow(new CreationFailureException("Bad data")).when(entityRepository).createEntity(any());

		HttpResponse<Object> response = notificationController.receiveNotification(testNotification);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatus(), "A bad request should be returned if nothing can be created.");
	}

	@ParameterizedTest
	@MethodSource("notificationStream")
	public void receiveNotification_badRequest_update(NotificationVO testNotification) throws Exception {

		doThrow(new UpdateFailureException("boom.")).when(entityRepository).updateEntity(any());

		HttpResponse<Object> response = notificationController.receiveNotification(testNotification);
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
								.setAdditionalProperties(Map.of("temp", new PropertyVO().type(PropertyVO.Type.PROPERTY).value(38)))));


		doThrow(new UpdateFailureException("404")).doNothing().when(entityRepository).updateEntity(any());

		HttpResponse<Object> response = notificationController.receiveNotification(testNotification);
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
								.setAdditionalProperties(Map.of("temp", new PropertyVO().type(PropertyVO.Type.PROPERTY).value(38)))));


		doThrow(new NoSuchEntityException("404")).when(entityRepository).updateEntity(any());
		doThrow(new CreationFailureException("Bad")).doNothing().when(entityRepository).createEntity(any());

		HttpResponse<Object> response = notificationController.receiveNotification(testNotification);
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
												.setAdditionalProperties(Map.of("temp", new PropertyVO().type(PropertyVO.Type.PROPERTY).value(38)))))
				),
				Arguments.of(new NotificationVO()
						.id(URI.create("urn:ngsi-ld:notification:test"))
						.type(NotificationVO.Type.NOTIFICATION)
						.data(List.of()))
		);
	}

}