package io.github.elliotwils0n.microservices.user.mapper;

import io.github.elliotwils0n.microservices.user.entity.User;
import io.github.elliotwils0n.microservices.user.model.UserRegistrationRequest;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public abstract class UserRegistrationRequestToUserMapper {

    @Autowired
    protected  PasswordEncoder passwordEncoder;

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "passwordHash", source = "password", qualifiedByName = "mapRawPasswordToPasswordHash")
    })
    public abstract User apply(UserRegistrationRequest userRegistrationRequest);

    @Named("mapRawPasswordToPasswordHash")
    String mapRawPasswordToPasswordHash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @AfterMapping
    void setInactive(@MappingTarget User user) {
        user.setActive(false);
    }

}
