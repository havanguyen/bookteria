package com.hanguyen.identity.controller;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import com.hanguyen.identity.dto.request.*;
import com.hanguyen.identity.dto.response.AuthenticationResponse;
import com.hanguyen.identity.service.AuthenticationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @NonFinal
    @Value("${app.security.cookie-secure}")
    boolean isCookieSecure;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request, HttpServletResponse response) {
        var result = authenticationService.authenticate(request);
        setCookies(response, result);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/outbound/authentication")
    ApiResponse<AuthenticationResponse> outboundAuthenticate(
            @RequestParam("code") String code, HttpServletResponse response) {
        AuthenticationResponse result = authenticationService.outboundAuthenticate(code);
        setCookies(response, result);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(
            @RequestBody(required = false) RefreshRequest request,
            @CookieValue(name = "REFRESH_TOKEN", required = false) String refreshToken,
            HttpServletResponse response) {
        if (request == null) {
            request = new RefreshRequest();
        }
        if (request.getToken() == null || request.getToken().isEmpty()) {
            request.setToken(refreshToken);
        }

        var result = authenticationService.refreshToken(request);
        setCookies(response, result);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(
            @RequestBody(required = false) LogoutRequest request,
            @CookieValue(name = "ACCESS_TOKEN", required = false) String accessToken,
            HttpServletResponse response) {
        if (request == null) {
            request = new LogoutRequest();
        }

        if ((request.getToken() == null || request.getToken().isEmpty()) && accessToken != null) {
            request.setToken(accessToken);
        }

        if (request.getToken() != null && !request.getToken().isEmpty()) {
            authenticationService.logout(request);
        }

        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", "")
                .httpOnly(true)
                .secure(isCookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", "")
                .httpOnly(true)
                .secure(isCookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ApiResponse.<Void>builder().build();
    }

    private void setCookies(HttpServletResponse response, AuthenticationResponse authResponse) {
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", authResponse.getToken())
                .httpOnly(true)
                .secure(isCookieSecure)
                .path("/")
                .maxAge(15 * 60)
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(isCookieSecure)
                .path("/")
                .maxAge(10 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}
