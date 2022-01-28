package org.fiware.notificationproxy.rest;

import org.fiware.test.model.EntityFragmentVO;
import org.fiware.test.model.EntityVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "jsr330")
public interface TestMapper {

	EntityFragmentVO entityVOToFragment(EntityVO entityVO);
}
