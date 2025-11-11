package com.hanguyen.file_service.controller;


import com.hanguyen.file_service.dto.ApiResponse;
import com.hanguyen.file_service.dto.response.UploadResponse;
import com.hanguyen.file_service.service.CloudinaryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@Slf4j
public class CloudinaryController {

    CloudinaryService cloudinaryService;

    @PostMapping("/upload/image")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<UploadResponse> uploadImage(@RequestParam("file") MultipartFile file ){

        String url = cloudinaryService.uploadFile(file);
        return ApiResponse.<UploadResponse>builder()
                .result(UploadResponse.builder()
                        .url(url)
                        .build())
                .build();
    }
}
