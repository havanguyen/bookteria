package com.hanguyen.notification.configuration;

import org.springframework.beans.factory.annotation.Value;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class BrevoRequestInterceptor implements RequestInterceptor {

    @Value("${brevo.mcp.brevo}")
    private String brevoToken;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("api-key", brevoToken);
    }
}
