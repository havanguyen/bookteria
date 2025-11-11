package com.hanguyen.identity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.hanguyen.identity.dto.request.RoleRequest;
import com.hanguyen.identity.dto.response.RoleResponse;
import com.hanguyen.identity.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
