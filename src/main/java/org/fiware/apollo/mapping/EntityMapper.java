package org.fiware.apollo.mapping;

import org.fiware.ngsi.model.EntityFragmentVO;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.apollo.model.NotifiedEntityVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jsr330")
public interface EntityMapper {

	String DEFAULT_CONTEXT = "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld";

	EntityVO notifiedEntityVOToEntityVO(NotifiedEntityVO notifiedEntityVO);

	default EntityVO fixedNotifiedEntityVOToEntityVO(NotifiedEntityVO notifiedEntityVO) {
		EntityVO entityVO = notifiedEntityVOToEntityVO(notifiedEntityVO);
		if(entityVO.atContext() == null) {
			// set default to fulfill the requirements for requests with application/ld+json
			entityVO.setAtContext(DEFAULT_CONTEXT);
		}
		return entityVO;
	}

	EntityFragmentVO entityVOToEntityFragmentVO(EntityVO entityVO);

	default EntityFragmentVO fixedEntityVOToEntityFragmentVO(EntityVO entityVO) {
		EntityFragmentVO entityFragmentVO = entityVOToEntityFragmentVO(entityVO);
		if(entityFragmentVO.atContext() == null) {
			// set default to fulfill the requirements for requests with application/ld+json
			entityFragmentVO.setAtContext(DEFAULT_CONTEXT);
		}
		return entityFragmentVO;
	}
}
