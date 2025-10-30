package com.hanguyen.identity.repository.httpclient;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.hanguyen.identity.configuration.InternalRequestInterceptor;
import com.hanguyen.identity.dto.request.ProfileCreationRequest;
import com.hanguyen.identity.dto.request.ProfileUpdateRequest;
import com.hanguyen.identity.dto.response.UserProfileResponse;

@FeignClient(
        name = "profile-service",
        url = "${app.service.profile}",
        configuration = {InternalRequestInterceptor.class})
public interface ProfileClient {
    @PostMapping(value = "/internal/users", produces = MediaType.APPLICATION_JSON_VALUE)
    UserProfileResponse createProfile(@RequestBody ProfileCreationRequest profileCreationRequest);

    @GetMapping(value = "/internal/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    UserProfileResponse getProfileByUserId(@PathVariable("userId") String userId);

    @PostMapping(value = "/internal/users/by-ids", produces = MediaType.APPLICATION_JSON_VALUE)
    List<UserProfileResponse> getProfilesByUserIds(@RequestBody List<String> userIds);

    @PutMapping(value = "/internal/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    UserProfileResponse updateProfileByUserId(
            @PathVariable("userId") String userId, @RequestBody ProfileUpdateRequest profileUpdateRequest);
}
