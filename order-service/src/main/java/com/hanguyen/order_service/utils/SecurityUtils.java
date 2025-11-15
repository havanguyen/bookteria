package com.hanguyen.order_service.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@Slf4j
public class SecurityUtils {

    static public String getUserId(){
       Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       return jwt.getSubject();
    }
}
