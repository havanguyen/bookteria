package com.hanguyen.identity.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hanguyen.identity.utils.KeyUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class JwkController {

    private final KeyUtils keyUtils;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() {
        return keyUtils.jwkSet().toJSONObject();
    }
}
