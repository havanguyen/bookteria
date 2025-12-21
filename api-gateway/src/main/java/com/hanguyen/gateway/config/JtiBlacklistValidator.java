package com.hanguyen.gateway.config;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JtiBlacklistValidator implements OAuth2TokenValidator<Jwt> {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Boolean isBlacklisted = redisTemplate.hasKey(token.getId()).block();

        if (Boolean.TRUE.equals(isBlacklisted)) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token is blacklisted", null));
        }
        return OAuth2TokenValidatorResult.success();
    }
}
