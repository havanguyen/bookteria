package com.hanguyen.gateway.config;

import java.util.Arrays;
import java.util.Collections;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;

import lombok.experimental.NonFinal;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    private final ReactiveStringRedisTemplate redisTemplate;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @NonFinal
    @Value("${app.api-prefix}")
    private String apiPrefix;

    @NonFinal
    private final String[] publicEndpoints = {
            "/identity/auth/**",
            "/identity/users/registration",
            "/identity/.well-known/jwks.json",
            "/notification/email/sent",
            "/search/**",
            "/inventory/**",
            "/payment/**",
            "/products", "/products/**", "/categories", "/categories/**", "/authors", "/authors/**", "/publishers",
            "/publishers/**"
    };

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder) {
        String[] formattedPublicEndpoints = Arrays.stream(publicEndpoints)
                .map(endpoint -> apiPrefix + endpoint)
                .toArray(String[]::new);

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(formattedPublicEndpoints).permitAll()
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(jwtDecoder))
                        .bearerTokenConverter(cookieTokenConverter()));

        return http.build();
    }

    private ServerAuthenticationConverter cookieTokenConverter() {
        return exchange -> {
            String token = null;
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            if (token == null) {
                var cookie = exchange.getRequest().getCookies().getFirst("ACCESS_TOKEN");
                if (cookie != null) {
                    log.info(cookie.getValue());
                    token = cookie.getValue();
                }
            }

            if (token != null) {
                return Mono.just(new BearerTokenAuthenticationToken(token));
            }
            return Mono.empty();
        };
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        log.info("jwkSetUri : {}", jwkSetUri);
        log.info("issuerUri : {}", issuerUri);

        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwtProcessorCustomizer(processor -> {
                    processor.setJWSTypeVerifier(
                            new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("at+jwt"), JOSEObjectType.JWT,
                                    null));
                })
                .build();

        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));

        return token -> jwtDecoder.decode(token)
                .doOnError(e -> {
                    if (e instanceof JwtValidationException jwtException) {
                        jwtException.getErrors().forEach(error -> log.error("   >> Chi tiết lỗi: {} - {}",
                                error.getErrorCode(), error.getDescription()));
                    }
                })

                .flatMap(jwt -> redisTemplate.hasKey(jwt.getId())
                        .doOnError(e -> log.error("❌ Lỗi kết nối Redis: {}", e.getMessage()))

                        .flatMap(isBlacklisted -> {
                            if (Boolean.TRUE.equals(isBlacklisted)) {
                                return Mono.error(new JwtValidationException(
                                        "Token is blacklisted",
                                        Collections.emptyList()));
                            }
                            return Mono.just(jwt);
                        }));
    }
}