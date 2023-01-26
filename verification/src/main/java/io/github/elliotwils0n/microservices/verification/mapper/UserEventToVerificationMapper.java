package io.github.elliotwils0n.microservices.verification.mapper;

import io.github.elliotwils0n.microservices.core.event.UserEvent;
import io.github.elliotwils0n.microservices.verification.entity.Verification;
import org.apache.commons.lang3.RandomStringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserEventToVerificationMapper {

    UserEventToVerificationMapper INSTANCE = Mappers.getMapper(UserEventToVerificationMapper.class);

    @Mapping(target = "userId", source = "userId")
    Verification apply(UserEvent userEvent);

    @AfterMapping
    default void updateTarget(@MappingTarget Verification verification) {
        verification.setUsed(false);
        verification.setHash(RandomStringUtils.random(100, true, true));
    }

}
