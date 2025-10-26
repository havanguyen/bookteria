package com.devteria.notification.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.devteria.notification.dto.request.ProfileCreationRequest;
import com.devteria.notification.dto.request.ProfileUpdateRequest;
import com.devteria.notification.dto.response.UserProfileResponse;
import com.devteria.notification.entity.UserProfile;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfile toUserProfile(ProfileCreationRequest profileCreationRequest);

    UserProfileResponse toUserProfileResponse(UserProfile userProfile);

    void updateUserProfile(@MappingTarget UserProfile userProfile, ProfileUpdateRequest request);
}
