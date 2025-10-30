package com.hanguyen.identity.configuration;

import org.springframework.beans.factory.annotation.Value;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class InternalRequestInterceptor implements RequestInterceptor {

    @Value("${app.internal-token}")
    private String internalToken;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("X-Internal-Token", internalToken);
    }
}
