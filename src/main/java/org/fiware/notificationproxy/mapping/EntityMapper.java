package org.fiware.notificationproxy.mapping;

import org.fiware.ngsi.model.EntityFragmentVO;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.notificationproxy.model.NotifiedEntityVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jsr330")
public interface EntityMapper {

	String DEFAULT_CONTEXT = "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld";

	default EntityVO mapToCleanedEntity(NotifiedEntityVO notifiedEntityVO) {
		EntityVO entityVO = notifiedEntityVOToEntityVO(notifiedEntityVO);
		if (entityVO.getLocation().getType() == null || entityVO.getLocation().getValue() == null) {
			entityVO.setLocation(null);
		}
		if (entityVO.getObservationSpace().getType() == null || entityVO.getObservationSpace().getValue() == null) {
			entityVO.setObservationSpace(null);
		}
		if (entityVO.getOperationSpace().getType() == null || entityVO.getOperationSpace().getValue() == null) {
			entityVO.setOperationSpace(null);
		}
		if (entityVO.getAtContext() == null) {
			entityVO.setAtContext(DEFAULT_CONTEXT);
		}

		return entityVO;
	}


	EntityVO notifiedEntityVOToEntityVO(NotifiedEntityVO entityVO);

	default EntityFragmentVO mapToCleanedFragment(EntityVO entityVO) {
		EntityFragmentVO entityFragmentVO = entityVOToEntityFragmentVO(entityVO);
		if (entityFragmentVO.getLocation() != null && entityFragmentVO.getLocation().getType() == null && entityFragmentVO.getLocation().getValue() == null) {
			entityFragmentVO.setLocation(null);
		}
		if (entityFragmentVO.getObservationSpace() != null && entityFragmentVO.getObservationSpace().getType() == null && entityFragmentVO.getObservationSpace().getValue() == null) {
			entityFragmentVO.setObservationSpace(null);
		}
		if (entityFragmentVO.getOperationSpace() != null && entityFragmentVO.getOperationSpace().getType() == null && entityFragmentVO.getOperationSpace().getValue() == null) {
			entityFragmentVO.setOperationSpace(null);
		}
		if (entityFragmentVO.getAtContext() == null) {
			entityFragmentVO.setAtContext(DEFAULT_CONTEXT);
		}

		return entityFragmentVO;
	}

	EntityFragmentVO entityVOToEntityFragmentVO(EntityVO entityVO);
}
