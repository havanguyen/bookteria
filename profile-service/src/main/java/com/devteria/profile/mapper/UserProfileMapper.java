package com.devteria.profile.mapper;

import com.devteria.profile.dto.request.ProfileUpdateRequest;
import org.mapstruct.Mapper;

import com.devteria.profile.dto.request.ProfileCreationRequest;
import com.devteria.profile.dto.response.UserProfileResponse;
import com.devteria.profile.entity.UserProfile;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfile toUserProfile(ProfileCreationRequest profileCreationRequest);

    UserProfileResponse toUserProfileResponse(UserProfile userProfile);

    void updateUserProfile(@MappingTarget UserProfile userProfile, ProfileUpdateRequest request);
}
