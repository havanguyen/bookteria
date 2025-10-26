package com.devteria.notification.controller;

import java.util.List;

import com.devteria.notification.dto.request.ProfileUpdateRequest;
import org.springframework.web.bind.annotation.*;

import com.devteria.notification.dto.request.ProfileCreationRequest;
import com.devteria.notification.dto.response.UserProfileResponse;
import com.devteria.notification.service.UserProfileRService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalUserProfileController {

    UserProfileRService userProfileRService;

    @PostMapping("/internal/users")
    UserProfileResponse creationProfile(@RequestBody ProfileCreationRequest profileCreationRequest) {
        return userProfileRService.createProfile(profileCreationRequest);
    }

    @GetMapping("/internal/users/{userId}")
    UserProfileResponse getProfileByUserId(@PathVariable String userId) {
        return userProfileRService.getProfileByUserId(userId);
    }

    @PostMapping("/internal/users/by-ids")
    List<UserProfileResponse> getProfilesByUserIds(@RequestBody List<String> userIds) {
        return userProfileRService.getProfilesByUserIds(userIds);
    }

    @PutMapping("/internal/users/{userId}")
    UserProfileResponse updateProfileByUserId(@PathVariable String userId, @RequestBody ProfileUpdateRequest request) {
        return userProfileRService.updateProfileByUserId(userId, request);
    }
}