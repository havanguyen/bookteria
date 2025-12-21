package com.hanguyen.profile.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hanguyen.profile.dto.request.ProfileCreationRequest;
import com.hanguyen.profile.dto.request.ProfileUpdateRequest;
import com.hanguyen.profile.dto.response.UploadResponse;
import com.hanguyen.profile.dto.response.UserProfileResponse;
import com.hanguyen.profile.entity.UserProfile;
import com.hanguyen.profile.mapper.UserProfileMapper;
import com.hanguyen.profile.repository.UserProfileRepository;
import com.hanguyen.profile.repository.httpClient.FileClient;
import com.hanguyen.profile.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileService {
    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;
    FileClient fileClient;

    @Transactional
    public UserProfileResponse createProfile(ProfileCreationRequest profileCreationRequest) {
        userProfileRepository.findByUserId(profileCreationRequest.getUserId()).ifPresent(p -> {
            log.warn("Profile already exists for userId: {}", profileCreationRequest.getUserId());
        });

        UserProfile userProfile = userProfileMapper.toUserProfile(profileCreationRequest);
        userProfile = userProfileRepository.save(userProfile);
        log.info("Created new profile with ID: {} for User ID: {}", userProfile.getId(), userProfile.getUserId());
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public UserProfileResponse getProfile(UUID id) {
        UserProfile userProfile =
                userProfileRepository.findById(id).orElseThrow(() -> new RuntimeException("Profile id not found"));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public List<UserProfileResponse> getAllProfiles() {
        var profiles = userProfileRepository.findAll();

        return profiles.stream().map(userProfileMapper::toUserProfileResponse).toList();
    }

    @Transactional
    public UserProfileResponse updateProfileByUserId(String userId, ProfileUpdateRequest request) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElseGet(() -> {
            log.info("Profile not found for userId: {}. Creating a new one.", userId);
            ProfileCreationRequest creationRequest = ProfileCreationRequest.builder()
                    .userId(userId)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .dob(request.getDob())
                    .city(request.getCity())
                    .avatarUrl(request.getAvatarUrl())
                    .build();
            return userProfileMapper.toUserProfile(creationRequest);
        });

        userProfileMapper.updateUserProfile(userProfile, request);
        userProfile.setUserId(userId);

        UserProfile savedProfile = userProfileRepository.save(userProfile);
        log.info("Updated/Created profile with ID: {} for User ID: {}", savedProfile.getId(), savedProfile.getUserId());
        return userProfileMapper.toUserProfileResponse(savedProfile);
    }

    @Transactional
    public UserProfileResponse updateAvatar(MultipartFile file) {

        String userId = SecurityUtils.getUserId();

        UploadResponse uploadResponse = fileClient.uploadImage(file);
        String avatarUrl = uploadResponse.getUrl();

        UserProfile userProfile =
                userProfileRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("Profile not found"));

        userProfile.setAvatarUrl(avatarUrl);
        userProfile = userProfileRepository.save(userProfile);

        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public void deleteProfile(UUID id) {
        userProfileRepository.deleteById(id);
    }

    public UserProfileResponse getProfileByUserId(String userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId).orElse(null);
        if (userProfile == null) {
            log.warn("No profile found for userId: {}", userId);
            return null;
        }
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public List<UserProfileResponse> getProfilesByUserIds(List<String> userIds) {
        var profiles = userProfileRepository.findByUserIdIn(userIds);
        return profiles.stream().map(userProfileMapper::toUserProfileResponse).collect(Collectors.toList());
    }
}
