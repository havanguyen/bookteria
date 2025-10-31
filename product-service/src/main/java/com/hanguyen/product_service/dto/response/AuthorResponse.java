package com.hanguyen.product_service.dto.response;

import lombok.Data;

@Data
public class AuthorResponse {
    private String id;
    private String name;
    private String bio;
    private String avatarUrl;
}