package com.devteria.notification.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;

public class BrevoRequestInterceptor implements RequestInterceptor {

    @Value("${brevo.mcp.brevo}")
    private String brevoToken;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("api-key", brevoToken);
    }
}
