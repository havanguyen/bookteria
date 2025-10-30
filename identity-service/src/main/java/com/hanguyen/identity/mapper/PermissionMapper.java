package com.hanguyen.identity.mapper;

import org.mapstruct.Mapper;

import com.hanguyen.identity.dto.request.PermissionRequest;
import com.hanguyen.identity.dto.response.PermissionResponse;
import com.hanguyen.identity.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
