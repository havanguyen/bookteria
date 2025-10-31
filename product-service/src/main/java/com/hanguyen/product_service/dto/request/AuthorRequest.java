package com.hanguyen.product_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AuthorRequest {
    @NotEmpty
    private String name;
    private String bio;
    private String avatarUrl;
}