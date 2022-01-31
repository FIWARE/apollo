package org.fiware.notificationproxy.repository;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.notificationproxy.configuration.GeneralProperties;
import org.fiware.notificationproxy.exception.CreationFailureException;
import org.fiware.notificationproxy.exception.NoSuchEntityException;
import org.fiware.notificationproxy.exception.UpdateFailureException;
import org.fiware.notificationproxy.mapping.EntityMapper;

import javax.inject.Singleton;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class EntityRepository {

	private final GeneralProperties generalProperties;
	private final EntitiesApiClient entitiesApiClient;
	private final EntityMapper entityMapper;

	public void updateEntity(EntityVO entityVO) throws NoSuchEntityException, UpdateFailureException {
		try {
			HttpResponse<Object> response = entitiesApiClient.appendEntityAttrs(entityVO.id(), entityMapper.mapToCleanedFragment(entityVO), generalProperties.getTenant(), null);
			switch (response.getStatus()) {
				case NO_CONTENT -> {
					return;
				}
				case MULTI_STATUS -> {
					log.info("Not all values where appended.");
					log.debug("Response was: {}", getFailureReason(response));
					return;
				}
				case NOT_FOUND -> throw new NoSuchEntityException(String.format("Entity %s does not exist.", entityVO.id()));
			}
		} catch (HttpClientResponseException e) {
			switch (e.getStatus()) {
				case NOT_FOUND -> throw new NoSuchEntityException(String.format("Entity %s does not exist.", entityVO.id()));
				default -> throw new UpdateFailureException(String.format("Was not able to update entity %s. Reason: %s", entityVO.id(), getFailureReason(e.getResponse())));
			}
		}
	}

	private Object getFailureReason(HttpResponse response) {
		return response.getBody().orElse("unknown");
	}

	public void createEntity(EntityVO entityVO) throws CreationFailureException {
		try {
			entitiesApiClient.createEntity(entityVO, generalProperties.getTenant());
			return;
		} catch (HttpClientResponseException e) {
			switch (e.getStatus()) {
				case CONFLICT -> throw new CreationFailureException(String.format("Entity %s already exists. Reason: %s", entityVO.id(), getFailureReason(e.getResponse())));
				default -> throw new CreationFailureException(String.format("Was not able to create entity %s. Status: %s,  Reason: %s", entityVO.id(), e.getStatus(), getFailureReason(e.getResponse())));
			}
		}
	}

}
