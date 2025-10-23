package com.devteria.profile.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import com.devteria.profile.dto.request.ProfileCreationRequest;
import com.devteria.profile.dto.request.ProfileUpdateRequest;
import com.devteria.profile.dto.response.UserProfileResponse;
import com.devteria.profile.service.UserProfileRService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserProfileController {

    UserProfileRService userProfileRService;

    @PostMapping("/users")
    UserProfileResponse creationProfile(@RequestBody ProfileCreationRequest profileCreationRequest) {
        return userProfileRService.createProfile(profileCreationRequest);
    }

    @GetMapping("/users/{profileId}")
    UserProfileResponse getProfile(@PathVariable UUID profileId) {
        return userProfileRService.getProfile(profileId);
    }

    @PutMapping("/users/{profileId}")
    UserProfileResponse updateProfile(@PathVariable UUID profileId, @RequestBody ProfileUpdateRequest request) {
        return userProfileRService.updateProfile(profileId, request);
    }

    @DeleteMapping("/users/{profileId}")
    String deleteProfile(@PathVariable UUID profileId) {
        userProfileRService.deleteProfile(profileId);
        return "Profile has been deleted";
    }
}
