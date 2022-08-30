package org.fiware.apollo.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.reactivex.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.apollo.api.NotificationApi;
import org.fiware.apollo.exception.CreationFailureException;
import org.fiware.apollo.exception.NoSuchEntityException;
import org.fiware.apollo.exception.UpdateFailureException;
import org.fiware.apollo.mapping.EntityMapper;
import org.fiware.apollo.model.NotificationVO;
import org.fiware.apollo.repository.EntityRepository;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of Apollo's NGSI-LD compatible notification api
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

	private final EntityMapper entityMapper;
	private final EntityRepository entityRepository;

	@Override
	public Single<HttpResponse<Object>> receiveNotification(NotificationVO notificationVO) {
		List<Single<Boolean>> resultList = notificationVO.getData()
				.stream()
				.map(entityMapper::fixedNotifiedEntityVOToEntityVO)
				.map(this::updateEntityInBroker)
				.toList();
		if (resultList == null || resultList.isEmpty()) {
			return Single.just(HttpResponse.badRequest());
		}
		return Single.zip(resultList, args -> {
			List<?> results = Arrays.asList(args);
			if (results.contains(true) && !results.contains(false)) {
				// everything succeeded
				return HttpResponse.noContent();
			} else if (results.contains(true) && results.contains(false)) {
				// some failed, some succeeded
				return HttpResponse.status(HttpStatus.MULTI_STATUS);
			} else {
				// everything failed
				return HttpResponse.badRequest();
			}
		});

	}

	// helper method to handle create or update, depending on the response from the context-broker
	// - update in case it already exists
	// - create if no such entity is found
	private Single<Boolean> updateEntityInBroker(EntityVO entityVO) {
		return entityRepository.updateEntity(entityVO)
				.flatMap(result -> switch (result) {
					case NOT_FOUND -> entityRepository.createEntity(entityVO);
					case UPDATED -> Single.just(true);
					default -> Single.just(false);
				});
	}
}
