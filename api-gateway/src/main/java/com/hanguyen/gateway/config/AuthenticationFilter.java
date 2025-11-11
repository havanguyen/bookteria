package com.hanguyen.gateway.config;

import com.hanguyen.gateway.dto.request.ApiResponse;
import com.hanguyen.gateway.service.IdentityService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {

    IdentityService identityService;
    ObjectMapper objectMapper;

    @NonFinal
    private String[] publicEndpoint = {
            "/identity/auth/.*",
            "/identity/users/registration",
            "/notification/email/sent",
            "/search/.*",
            "/inventory/.*",
            "/payment/.*" ,
            "/products", "/products/.*", "/categories", "/categories/.*", "/authors", "/authors/.*", "/publishers", "/publishers/.*"
    };

    @Value("${app.api-prefix}")
    @NonFinal
    String apiPrefix;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

       if (isPublicEndpoint(exchange.getRequest())) {
          return   chain.filter(exchange);
       }
        List<String> authHeader =
                exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);

        if (CollectionUtils.isEmpty(authHeader)) {
            return unauthenticated(exchange.getResponse());
        }
        String token = authHeader.getFirst().replace("Bearer ", "");
        log.info("Token: {}", token);

        return identityService.introspect(token)
                .flatMap(introspectResponse -> {
                    log.info("Introspection result: {}", introspectResponse.getResult().isValid());
                    if (introspectResponse.getResult().isValid()) {
                        return chain.filter(exchange);
                    } else {

                        return unauthenticated(exchange.getResponse());
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("Error during token introspection", throwable);
                    return unauthenticated(exchange.getResponse());
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(ServerHttpRequest request){
       return Arrays.stream(publicEndpoint).anyMatch(s ->
                request.getURI().getPath().matches(apiPrefix+s));
    }

    private Mono<Void> unauthenticated(ServerHttpResponse httpResponse) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(1401)
                .message("Unauthenticated")
                .build();

        return Mono.fromCallable(() -> objectMapper.writeValueAsString(apiResponse))
                .flatMap(body -> {
                    httpResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
                    httpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
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