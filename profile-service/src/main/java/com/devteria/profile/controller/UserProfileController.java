package com.devteria.profile.controller;

import java.util.List;
import java.util.UUID;

import com.devteria.profile.dto.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
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
    ApiResponse<UserProfileResponse> getProfile(@PathVariable String profileId) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileRService.getProfile(UUID.fromString(profileId)))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    ApiResponse<List<UserProfileResponse>> getAllProfiles() {
        return ApiResponse.<List<UserProfileResponse>>builder()
                .result(userProfileRService.getAllProfiles())
                .build();
    }

    @PutMapping("/users/{profileId}")
    ApiResponse<UserProfileResponse> updateProfile(@PathVariable String profileId, @RequestBody ProfileUpdateRequest request) {
      return  ApiResponse.<UserProfileResponse>builder()
                .result(userProfileRService.
                                updateProfile(UUID.fromString(profileId), request))
                .build();
    }

    @DeleteMapping("/users/{profileId}")
    ApiResponse<String> deleteProfile(@PathVariable String profileId) {
        userProfileRService.deleteProfile(UUID.fromString(profileId));
        return ApiResponse.<String>builder().result("Profile has been deleted").build();
    }
}
