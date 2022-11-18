package org.fiware.apollo.repository;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiware.apollo.rest.UpdateResult;
import org.fiware.ngsi.api.EntitiesApiClient;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.apollo.configuration.GeneralProperties;
import org.fiware.apollo.exception.CreationFailureException;
import org.fiware.apollo.mapping.EntityMapper;

import javax.inject.Singleton;

/**
 * Repository for abstracting the interface to the context broker
 */
@Slf4j
@Singleton
@RequiredArgsConstructor
public class EntityRepository {

	private final GeneralProperties generalProperties;
	private final EntitiesApiClient entitiesApiClient;
	private final EntityMapper entityMapper;

	/**
	 * Update the entity at the context broker
	 *
	 * @param entityVO entity to update
	 * @return a single, containing the result of the update
	 */
	public Single<UpdateResult> updateEntity(EntityVO entityVO) {

		try {
			return entitiesApiClient.appendEntityAttrs(entityVO.id(), entityMapper.fixedEntityVOToEntityFragmentVO(entityVO), generalProperties.getTenant(), null)
					.map(response -> {
						switch (response.getStatus()) {
							case NOT_FOUND -> {
								log.info("Entity {} does not exist.", entityVO.id());
								return UpdateResult.NOT_FOUND;
							}
							case NO_CONTENT -> {
								return UpdateResult.UPDATED;
							}
							case MULTI_STATUS -> {
								log.info("Not all values where appended.");
								log.debug("Response was: {}", getFailureReason(response));
								return UpdateResult.UPDATED;
							}
							default -> {
								log.info("Received unspecified ok state, will continue.");
								return UpdateResult.UPDATED;
							}
						}
					})
					.onErrorReturn(e -> {
						if (e instanceof HttpClientResponseException clientException) {
							if (clientException.getStatus().equals(HttpStatus.NOT_FOUND)) {
								log.info("Entity {} does not exist.", entityVO.id());
								return UpdateResult.NOT_FOUND;
							}
						}
						log.warn("Was not able to update entity {}.", entityVO.id(), e);
						return UpdateResult.ERROR;
					});
		} catch (HttpClientResponseException clientException) {
			if (clientException.getStatus().equals(HttpStatus.NOT_FOUND)) {
				log.info("Entity {} does not exist.", entityVO.id());
				return Single.just(UpdateResult.NOT_FOUND);
			}
			log.warn("Was not able to update entity {}. Reason: {}", entityVO.id(), getFailureReason(clientException.getResponse()));
			return Single.just(UpdateResult.ERROR);
		}

	}


	/**
	 * Create the entity on the context broker
	 *
	 * @param entityVO entity to be created
	 * @return A single emitting the creation result
	 */
	public Single<Boolean> createEntity(EntityVO entityVO) throws CreationFailureException {
		try {
			return entitiesApiClient
					.createEntity(entityVO, generalProperties.getTenant())
					.map(response -> {
						if (response.getStatus().getCode() >= 200 && response.getStatus().getCode() < 300) {
							log.info("Received unspecified ok state, will continue.");
							return true;
						} else {
							log.warn("Was not able to create entity {}. Status: {},  Reason: {}", entityVO.id(), response.getStatus(), getFailureReason(response));
							return false;
						}
					});
		} catch (HttpClientResponseException clientException) {
			if (clientException.getStatus().equals(HttpStatus.CONFLICT)) {
				log.warn("Entity {} already exists. Reason: {}", entityVO.id(), getFailureReason(clientException.getResponse()));
			} else {
				log.warn("Was not able to create entity {}. Status: {},  Reason: {}", entityVO.id(), clientException.getStatus(), getFailureReason(clientException.getResponse()));
			}
			return Single.just(false);
		}
	}

	// helper method to provide debugging information
	private Object getFailureReason(HttpResponse response) {
		return response.getBody().orElse("unknown");
	}

}