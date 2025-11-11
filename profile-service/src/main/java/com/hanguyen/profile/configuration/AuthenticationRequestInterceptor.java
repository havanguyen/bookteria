package com.hanguyen.profile.configuration;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthenticationRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String authHeader;
        if (attributes != null) {
            authHeader = attributes.getRequest().getHeader("Authorization");

            if (authHeader != null) {
                requestTemplate.header("Authorization", authHeader);
            }
        }
    }
}
