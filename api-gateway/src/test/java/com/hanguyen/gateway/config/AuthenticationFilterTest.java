package com.hanguyen.gateway.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class AuthenticationFilterTest {

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    @Mock
    private GatewayFilterChain chain;

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AuthenticationFilter authenticationFilter;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(authenticationFilter, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(authenticationFilter, "apiPrefix", "/api/v1");
    }

    @Test
    void filter_publicEndpoint_bypassAuth() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/identity/auth/token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(authenticationFilter.filter(exchange, chain))
                .verifyComplete();

        verify(chain, times(1)).filter(exchange);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void filter_validToken_authenticated() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("validToken")).thenReturn(Mono.just("jwtContent"));
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        StepVerifier.create(authenticationFilter.filter(exchange, chain))
                .verifyComplete();

        ArgumentCaptor<ServerWebExchange> captor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captor.capture());

        ServerWebExchange capturedExchange = captor.getValue();
        assertEquals("Bearer jwtContent",
                capturedExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void filter_invalidToken_unauthenticated() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalidToken")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("invalidToken")).thenReturn(Mono.empty());

        StepVerifier.create(authenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_noToken_unauthenticated() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users/me")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(authenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verifyNoInteractions(redisTemplate);
    }
}
