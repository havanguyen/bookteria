package com.hanguyen.order_service.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;

public class InternalRequestInterceptor implements RequestInterceptor {

    @Value("${app.internal-token}")
    private String internalToken;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("X-Internal-Token", internalToken);
    }
}
