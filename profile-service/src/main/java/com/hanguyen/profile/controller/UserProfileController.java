package com.hanguyen.profile.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hanguyen.profile.dto.request.ProfileUpdateRequest;
import com.hanguyen.profile.dto.response.ApiResponse;
import com.hanguyen.profile.dto.response.UserProfileResponse;
import com.hanguyen.profile.service.UserProfileRService;

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
        try {
            UUID uuid = UUID.fromString(profileId);
            return ApiResponse.<UserProfileResponse>builder()
                    .result(userProfileRService.getProfile(uuid))
                    .build();
        } catch (IllegalArgumentException e) {
            return ApiResponse.<UserProfileResponse>builder()
                    .code(9999)
                    .message("Invalid Profile ID format")
                    .build();
        }
    }

    @PostMapping("/users/avatar")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UserProfileResponse> updateMyAvatar(@RequestParam("file") MultipartFile file) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileRService.updateAvatar(file))
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    ApiResponse<List<UserProfileResponse>> getAllProfiles() {
        return ApiResponse.<List<UserProfileResponse>>builder()
                .result(userProfileRService.getAllProfiles())
                .build();
    }

    @PutMapping("/users/by-user/{userId}")
    ApiResponse<UserProfileResponse> updateProfileByUserId(
            @PathVariable String userId, @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.<UserProfileResponse>builder()
                .result(userProfileRService.updateProfileByUserId(userId, request))
                .build();
    }

    @DeleteMapping("/users/{profileId}")
    ApiResponse<String> deleteProfile(@PathVariable String profileId) {
        try {
            UUID uuid = UUID.fromString(profileId);
            userProfileRService.deleteProfile(uuid);
            return ApiResponse.<String>builder()
                    .result("Profile has been deleted")
                    .build();
        } catch (IllegalArgumentException e) {
            return ApiResponse.<String>builder()
                    .code(9999)
                    .message("Invalid Profile ID format for deletion")
                    .build();
        }
    }
}
