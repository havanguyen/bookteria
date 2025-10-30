package com.hanguyen.profile.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.hanguyen.profile.dto.request.ProfileCreationRequest;
import com.hanguyen.profile.dto.request.ProfileUpdateRequest;
import com.hanguyen.profile.dto.response.UserProfileResponse;
import com.hanguyen.profile.entity.UserProfile;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfile toUserProfile(ProfileCreationRequest profileCreationRequest);

    UserProfileResponse toUserProfileResponse(UserProfile userProfile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    void updateUserProfile(@MappingTarget UserProfile userProfile, ProfileUpdateRequest request);
}
