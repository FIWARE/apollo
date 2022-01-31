package org.fiware.notificationproxy.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.RequiredArgsConstructor;
import org.awaitility.Awaitility;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.notificationproxy.configuration.GeneralProperties;
import org.fiware.notificationproxy.mapping.EntityMapper;
import org.fiware.test.api.SubscriptionsApiClient;
import org.fiware.test.api.EntitiesTestApiClient;
import org.fiware.test.model.EndpointVO;
import org.fiware.test.model.EntityInfoVO;
import org.fiware.test.model.EntityVO;
import org.fiware.test.model.NotificationParamsVO;
import org.fiware.test.model.PropertyVO;
import org.fiware.test.model.SubscriptionVO;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration test to be used with the compose setup, provided at env/docker-compose.yaml
 * It expects the setup to be already running and communicates with both brokers deployed there.
 */
@MicronautTest
@RequiredArgsConstructor
class NotificationControllerIntegrationTest {

	private final EntitiesApiClient subscriberClient;
	private final SubscriptionsApiClient notifierSubscriptionClient;
	private final EntitiesTestApiClient notifierEntitiesClient;
	private final TestMapper entityMapper;
	private final GeneralProperties generalProperties;

	@Test
	public void test_success() {
		String cattleName = "urn:ngsi-ld:cattle:" + UUID.randomUUID().toString();
		EndpointVO endpointVO = new EndpointVO();
		endpointVO.uri(URI.create("http://localhost:8080/notification"));

		NotificationParamsVO notificationParamsVO = new NotificationParamsVO();
		notificationParamsVO.endpoint(endpointVO);

		EntityInfoVO entityInfoVO = new EntityInfoVO();
		entityInfoVO.setType("Cattle");

		SubscriptionVO subscriptionVO = new SubscriptionVO();
		subscriptionVO.atContext(EntityMapper.DEFAULT_CONTEXT);
		subscriptionVO.name("mySubscription");
		subscriptionVO.id(URI.create("urn:ngsi-ld:subscription:test-sub"));
		subscriptionVO.type(SubscriptionVO.Type.SUBSCRIPTION);
		subscriptionVO.notification(notificationParamsVO);
		subscriptionVO.entities(List.of(entityInfoVO));
		// workaround for the serializer
		subscriptionVO.geoQ(null);
		try {
			notifierSubscriptionClient.createSubscription(subscriptionVO);
		} catch (HttpClientResponseException e) {
			if (e.getStatus().equals(HttpStatus.CONFLICT)) {
				// ignore
			} else {
				fail();
			}
		}
		EntityVO testCattle = new EntityVO();
		testCattle.type("Cattle");
		testCattle.atContext(EntityMapper.DEFAULT_CONTEXT);
		testCattle.id(URI.create(cattleName));
		// workaround for the serializer
		testCattle.location(null);
		testCattle.observationSpace(null);
		testCattle.operationSpace(null);

		PropertyVO cattleTemp = new PropertyVO();
		cattleTemp.type(PropertyVO.Type.PROPERTY);
		cattleTemp.value(37);
		testCattle.setAdditionalProperties("temp", cattleTemp);
		try {
			notifierEntitiesClient.createEntity(testCattle);
		} catch (HttpClientResponseException e) {
			if (e.getStatus().equals(HttpStatus.CONFLICT)) {
				// ignore
			} else {
				fail();
			}
		}
		Awaitility.await("Wait for entity created in the subscriber.").atMost(Duration.of(10, ChronoUnit.SECONDS)).until(() -> {
			HttpResponse<org.fiware.ngsi.model.EntityVO> response = subscriberClient.retrieveEntityById(testCattle.getId(), generalProperties.getTenant(), null, null, null, null);
			return response.getStatus().equals(HttpStatus.OK);
		});


		cattleTemp.value(38);
		notifierEntitiesClient.appendEntityAttrs(testCattle.id(), entityMapper.entityVOToFragment(testCattle), null);
		Awaitility.await("Wait for entity to be updated in the subscriber.").atMost(Duration.of(10, ChronoUnit.SECONDS)).until(() -> {
			HttpResponse<org.fiware.ngsi.model.EntityVO> response = subscriberClient.retrieveEntityById(testCattle.getId(), generalProperties.getTenant(), null, null, null, null);

			if (response.getStatus().equals(HttpStatus.OK)) {
				if (((Map) response.getBody().get().getAdditionalProperties().get("temp")).get("value").equals(38)) {
					return true;
				}
			}
			return false;
		});

		// type updates are not supported, without we get 204 instead of 207
		cattleTemp.type(null);
		cattleTemp.value(39);
		notifierEntitiesClient.appendEntityAttrs(testCattle.id(), entityMapper.entityVOToFragment(testCattle), null);
		Awaitility.await("Wait for entity to be updated in the subscriber.").atMost(Duration.of(10, ChronoUnit.SECONDS)).until(() -> {
			HttpResponse<org.fiware.ngsi.model.EntityVO> response = subscriberClient.retrieveEntityById(testCattle.getId(), generalProperties.getTenant(), null, null, null, null);

			if (response.getStatus().equals(HttpStatus.OK)) {
				if (((Map) response.getBody().get().getAdditionalProperties().get("temp")).get("value").equals(39)) {
					return true;
				}
			}
			return false;
		});
	}

}