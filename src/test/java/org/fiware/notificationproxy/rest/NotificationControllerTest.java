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
import org.fiware.test.model.EntityVO;
import org.fiware.test.model.NotificationParamsVO;
import org.fiware.test.model.PropertyVO;
import org.fiware.test.model.SubscriptionVO;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.fail;

@MicronautTest
@RequiredArgsConstructor
class NotificationControllerTest {

	private final EntitiesApiClient subscriberClient;
	private final SubscriptionsApiClient notifierSubscriptionClient;
	private final EntitiesTestApiClient notifierEntitiesClient;
	private final TestMapper entityMapper;
	private final GeneralProperties generalProperties;
	private static final String CONTEXT = "https://raw.githubusercontent.com/smart-data-models/dataModel.DistributedLedgerTech/master/context.jsonld";

	@Test
	public void test_success() {
		String cattleName = "urn:ngsi-ld:cattle:" + UUID.randomUUID().toString();
		EndpointVO endpointVO = new EndpointVO();
		endpointVO.uri(URI.create("http://localhost:8080/notification"));

		NotificationParamsVO notificationParamsVO = new NotificationParamsVO();
		notificationParamsVO.endpoint(endpointVO);

		SubscriptionVO subscriptionVO = new SubscriptionVO();
		subscriptionVO.atContext(CONTEXT);
		subscriptionVO.name("mySubscription");
		subscriptionVO.id(URI.create("urn:ngsi-ld:subscription:test-sub"));
		subscriptionVO.type(SubscriptionVO.Type.SUBSCRIPTION);
		subscriptionVO.notification(notificationParamsVO);
		subscriptionVO.watchedAttributes(Set.of("temp"));
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
		testCattle.atContext(CONTEXT);
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
		notifierEntitiesClient.appendEntityAttrs(testCattle.id(), entityMapper.entityVOToFragment(testCattle), null);
		Awaitility.await("Wait for entity created in the subscriber.").atMost(Duration.of(10, ChronoUnit.SECONDS)).until(() -> {
			HttpResponse<org.fiware.ngsi.model.EntityVO> response = subscriberClient.retrieveEntityById(testCattle.getId(), generalProperties.getTenant(), null, null, null, null);
			return response.getStatus().equals(HttpStatus.OK);
		});

		int updatedTemp = 38;

		cattleTemp.value(updatedTemp);
		Awaitility.await("Wait for entity to be updated in the subscriber.").atMost(Duration.of(10, ChronoUnit.SECONDS)).until(() -> {
			HttpResponse<org.fiware.ngsi.model.EntityVO> response = subscriberClient.retrieveEntityById(testCattle.getId(), generalProperties.getTenant(), null, null, null, null);

			if(response.getStatus().equals(HttpStatus.OK)) {
				if((Integer) response.getBody().get().getAdditionalProperties().get("temp") == updatedTemp){
					return true;
				}
			}
			return false;
		});
	}

}