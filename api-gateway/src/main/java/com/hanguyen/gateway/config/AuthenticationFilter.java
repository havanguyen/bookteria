package com.hanguyen.gateway.config;

import com.hanguyen.gateway.dto.request.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {

    ReactiveStringRedisTemplate redisTemplate;
    ObjectMapper objectMapper;

    @NonFinal
    String[] publicEndpoint = {
            "/identity/auth/.*",
            "/identity/users/registration",
            "/identity/.well-known/jwks.json",
            "/notification/email/sent",
            "/search/.*",
            "/inventory/.*",
            "/payment/.*",
            "/products", "/products/.*", "/categories", "/categories/.*", "/authors", "/authors/.*", "/publishers",
            "/publishers/.*"
    };

    @Value("${app.api-prefix}")
    @NonFinal
    String apiPrefix;

    @Value("${spring.data.redis.host}")
    @NonFinal
    String redisHost;

    @PostConstruct
    public void init() {
        log.info("AuthenticationFilter initialized. Connected to Redis Host: {}", redisHost);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        if (isPublicEndpoint(exchange.getRequest())) {
            return chain.filter(exchange);
        }
        String token = null;
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);

        if (!CollectionUtils.isEmpty(authHeader)) {
            token = authHeader.getFirst().replace("Bearer ", "").trim();
        } else {
            var cookies = exchange.getRequest().getCookies();
            if (cookies.containsKey("ACCESS_TOKEN")) {
                token = Objects.requireNonNull(cookies.getFirst("ACCESS_TOKEN")).getValue();
            }
        }

        if (token == null) {
            return unauthenticated(exchange.getResponse());
        }

        log.info("Token: {}", token);

        return redisTemplate.opsForValue().get(token)
                .flatMap(jwt -> {
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(exchange.getRequest().mutate()
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                                    .build())
                            .build();
                    return chain.filter(mutatedExchange);
                })
                .switchIfEmpty(Mono.defer(() -> unauthenticated(exchange.getResponse())));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return Arrays.stream(publicEndpoint).anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }

    private Mono<Void> unauthenticated(ServerHttpResponse httpResponse) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(1401)
                .message("Unauthenticated")
                .build();

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(apiResponse))
                .flatMap(body -> {
                    httpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
                    try {
                        httpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    } catch (UnsupportedOperationException e) {
                        log.warn("Failed to set Content-Type header: {}", e.getMessage());
                    }
                    DataBuffer buffer = httpResponse.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
                    return httpResponse.writeWith(Mono.just(buffer));
                })
                .onErrorResume(JsonProcessingException.class, exception -> {
                    log.error("Error writing unauthenticated response", exception);
                    httpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return httpResponse.setComplete();
                });
    }
}