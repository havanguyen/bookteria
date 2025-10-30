package com.hanguyen.profile.configuration;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalRequestFilter extends OncePerRequestFilter {

    @Value("${app.internal-token}")
    private String expectedToken;

    private static final String[] INTERNAL_ENDPOINTS = {"/internal/users", "/internal/users/**"};

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        boolean isInternalEndpoint = false;
        for (String endpoint : INTERNAL_ENDPOINTS) {
            if (path.startsWith(endpoint)) {
                isInternalEndpoint = true;
                break;
            }
        }

        if (!isInternalEndpoint) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader("X-Internal-Token");

        if (token == null || !token.equals(expectedToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or missing X-Internal-Token");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
