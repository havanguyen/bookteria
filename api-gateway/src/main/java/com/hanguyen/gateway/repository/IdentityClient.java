package com.hanguyen.gateway.repository;


import com.hanguyen.gateway.dto.request.ApiResponse;
import com.hanguyen.gateway.dto.request.IntrospectRequest;
import com.hanguyen.gateway.dto.response.IntrospectResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;


@FeignClient(name = "identity-service" )
public interface IdentityClient {
    @PostExchange(url = "/auth/introspect" , contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<IntrospectResponse>> introspect(
            @RequestBody IntrospectRequest introspectRequest
    );
}
