package com.github.sportbot.mapper;

import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", imports = LocalDateTime.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "exerciseRecords", ignore = true)
    @Mapping(target = "maxHistory", ignore = true)
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "programs", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    User toEntity(RegistrationRequest request);
}
