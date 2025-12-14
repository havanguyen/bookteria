package com.hanguyen.identity.repository.httpclient;

import jakarta.ws.rs.core.MediaType;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import com.hanguyen.identity.dto.request.ExchangeTokenRequest;
import com.hanguyen.identity.dto.response.ExchangeTokenResponse;

import feign.QueryMap;

@FeignClient(name = "outbound-identity", url = "https://oauth2.googleapis.com")
public interface OutboundIdentityClient {
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED)
    ExchangeTokenResponse exchangeToken(@QueryMap ExchangeTokenRequest exchangeTokenRequest);
}
