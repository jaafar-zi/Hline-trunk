package com.example.demo.domain.userManagementDomain.role.mapper;

import com.example.demo.domain.userManagementDomain.role.dto.PermissionDTO;
import com.example.demo.domain.userManagementDomain.role.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    PermissionMapper INSTANCE = Mappers.getMapper(PermissionMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    PermissionDTO toDto(Permission permission);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    Permission toEntity(PermissionDTO dto);
}
