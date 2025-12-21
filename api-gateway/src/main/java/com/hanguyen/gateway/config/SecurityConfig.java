package com.hanguyen.gateway.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;

import lombok.experimental.NonFinal;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

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
    public ReactiveJwtDecoder jwtDecoder(JtiBlacklistValidator blacklistValidator) {
        NimbusReactiveJwtDecoder jwtDecoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withBlacklist = new DelegatingOAuth2TokenValidator<>(withIssuer, blacklistValidator);

        jwtDecoder.setJwtValidator(withBlacklist);

        return jwtDecoder;
    }
}