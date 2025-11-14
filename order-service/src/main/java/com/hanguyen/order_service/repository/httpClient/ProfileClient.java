package com.hanguyen.order_service.repository.httpClient;


import com.hanguyen.order_service.configuration.InternalRequestInterceptor;
import com.hanguyen.order_service.dto.response.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "profile-service", configuration ={
        InternalRequestInterceptor.class
})
public interface ProfileClient {
    @GetMapping(value = "/profile/internal/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    UserProfileResponse getProfileByUserId(@PathVariable("userId") String userId);
}
