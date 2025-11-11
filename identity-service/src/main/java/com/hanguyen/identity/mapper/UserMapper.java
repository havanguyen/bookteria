package com.hanguyen.identity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hanguyen.identity.dto.request.ProfileCreationRequest;
import com.hanguyen.identity.dto.request.UserCreationRequest;
import com.hanguyen.identity.dto.request.UserUpdateRequest;
import com.hanguyen.identity.dto.response.UserResponse;
import com.hanguyen.identity.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    @Mapping(source = "active", target = "active")
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

    ProfileCreationRequest toProfileCreationRequest(UserCreationRequest request);
}
