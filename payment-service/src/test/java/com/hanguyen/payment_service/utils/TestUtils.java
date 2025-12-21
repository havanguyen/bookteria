package com.hanguyen.payment_service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;

public class TestUtils {
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    public static <T> T getObject(String fileName, Class<T> tClass) {
        try {
            InputStream inputStream = TestUtils.class.getClassLoader().getResourceAsStream(fileName);
            return objectMapper.readValue(inputStream, tClass);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load JSON file: " + fileName, e);
        }
    }
}
