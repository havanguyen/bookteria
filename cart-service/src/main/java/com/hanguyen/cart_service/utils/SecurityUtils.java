package com.hanguyen.cart_service.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

public class SecurityUtils {

    static public String getUserId(){
       Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       return jwt.getSubject();
    }
}
