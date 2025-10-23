package com.devteria.profile.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/users/{profileId}")
    UserProfileResponse getProfile(@PathVariable String profileId) {
        return userProfileRService.getProfile(UUID.fromString(profileId));
    }

    @GetMapping("/users")
    List<UserProfileResponse> getAllProfiles() {
        return userProfileRService.getAllProfiles();
    }

    @PutMapping("/users/{profileId}")
    UserProfileResponse updateProfile(@PathVariable String profileId, @RequestBody ProfileUpdateRequest request) {
        return userProfileRService.updateProfile(UUID.fromString(profileId), request);
    }

    @DeleteMapping("/users/{profileId}")
    String deleteProfile(@PathVariable String profileId) {
        userProfileRService.deleteProfile(UUID.fromString(profileId));
        return "Profile has been deleted";
    }
}
