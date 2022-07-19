package org.fiware.apollo.mapping;

import org.fiware.ngsi.model.EntityFragmentVO;
import org.fiware.ngsi.model.EntityVO;
import org.fiware.apollo.model.NotifiedEntityVO;
import org.mapstruct.Mapper;

/**
 * Mapper interface(to be handled by mapstruct) for translating between NGSI-LD entities and the Apollo-Domain.
 */
@Mapper(componentModel = "jsr330")
public interface EntityMapper {

	String DEFAULT_CONTEXT = "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld";

	/**
	 * Translate an entity as notified to the NGSI-KD model
	 * @param notifiedEntityVO the entity received via notification
	 * @return the corresponding NGSI-LD entity
	 */
	EntityVO notifiedEntityVOToEntityVO(NotifiedEntityVO notifiedEntityVO);

	/**
	 * Wrapper method for appending the context to ngsi-entities after plain translation
	 * @param notifiedEntityVO the entity received via notification
	 * @return the corresponding NGSI-LD entity
	 */
	default EntityVO fixedNotifiedEntityVOToEntityVO(NotifiedEntityVO notifiedEntityVO) {
		EntityVO entityVO = notifiedEntityVOToEntityVO(notifiedEntityVO);
		if(entityVO.atContext() == null) {
			// set default to fulfill the requirements for requests with application/ld+json
			entityVO.setAtContext(DEFAULT_CONTEXT);
		}
		return entityVO;
	}

	/**
	 * Translates a full NGSI-LD entity to an Entity-Fragment, as mandated by the update-api
	 * @param entityVO the entity to be translated
	 * @return corresponding entity fragment
	 */
	EntityFragmentVO entityVOToEntityFragmentVO(EntityVO entityVO);

	/**
	 * Wrapper method for appending the context to ngsi-entity fragments after plain translation
	 * @param entityVO the full entity
	 * @return the corresponding NGSI-LD entity fragment
	 */
	default EntityFragmentVO fixedEntityVOToEntityFragmentVO(EntityVO entityVO) {
		EntityFragmentVO entityFragmentVO = entityVOToEntityFragmentVO(entityVO);
		if(entityFragmentVO.atContext() == null) {
			// set default to fulfill the requirements for requests with application/ld+json
			entityFragmentVO.setAtContext(DEFAULT_CONTEXT);
		}
		return entityFragmentVO;
	}
}
