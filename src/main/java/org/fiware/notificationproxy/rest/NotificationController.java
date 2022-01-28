package org.fiware.notificationproxy.rest;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.notificationproxy.api.NotificationApi;
import org.fiware.notificationproxy.exception.CreationFailureException;
import org.fiware.notificationproxy.exception.NoSuchEntityException;
import org.fiware.notificationproxy.exception.UpdateFailureException;
import org.fiware.notificationproxy.mapping.EntityMapper;
import org.fiware.notificationproxy.model.NotificationVO;
import org.fiware.notificationproxy.repository.EntityRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

	private final EntityMapper entityMapper;
	private final EntityRepository entityRepository;

	@Override
	public HttpResponse<Object> receiveNotification(NotificationVO notificationVO) {

		List<Boolean> resultList = notificationVO.getData().stream().map(entityMapper::mapToCleanedEntity).map(this::updateEntityInBroker).collect(Collectors.toList());
		if (resultList.contains(true) && !resultList.contains(false)) {
			// everything succeeded
			return HttpResponse.noContent();
		} else if (resultList.contains(true) && resultList.contains(false)) {
			// some failed, some succeeded
			return HttpResponse.status(HttpStatus.MULTI_STATUS);
		} else {
			// everything failed
			return HttpResponse.badRequest();
		}
	}

	private boolean updateEntityInBroker(EntityVO entityVO) {
		try {
			entityRepository.updateEntity(entityVO);
		} catch (NoSuchEntityException e) {
			try {
				entityRepository.createEntity(entityVO);
			} catch (CreationFailureException ex) {
				log.warn("Was not able to create entity {}.", entityVO.id(), ex);
				return false;
			}
		} catch (UpdateFailureException e) {
			log.warn("Was not able to update entity {}.", entityVO.id(), e);
			return false;
		}
		return true;
	}
}
