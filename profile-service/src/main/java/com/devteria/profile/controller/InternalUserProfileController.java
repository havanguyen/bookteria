package com.devteria.profile.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.devteria.profile.dto.request.ProfileCreationRequest;
import com.devteria.profile.dto.response.UserProfileResponse;
import com.devteria.profile.service.UserProfileRService;

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
}