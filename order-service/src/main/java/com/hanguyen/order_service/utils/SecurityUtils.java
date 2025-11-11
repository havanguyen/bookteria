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

    static public String getIpAddress(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://api.ipify.org")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assert response.body() != null;
            log.info("ip address {}" , response.body().string());
            return response.body().string();
        } catch (Exception e) {
            return "171.227.4.230";
        }
    }
}
