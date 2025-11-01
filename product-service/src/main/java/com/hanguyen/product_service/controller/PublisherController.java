package com.hanguyen.product_service.controller;

import com.hanguyen.product_service.dto.request.PublisherRequest;
import com.hanguyen.product_service.dto.response.ApiResponse;
import com.hanguyen.product_service.dto.response.AuthorResponse;
import com.hanguyen.product_service.dto.response.PublisherResponse;
import com.hanguyen.product_service.service.PublisherService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/publishers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class PublisherController {

    PublisherService publisherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PublisherResponse> create(@RequestBody @Valid PublisherRequest request) {
        return ApiResponse.<PublisherResponse>builder()
                .result(publisherService.create(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<PublisherResponse>> getAll(Pageable pageable) {
        return ApiResponse.<Page<PublisherResponse>>builder()
                .result(publisherService.getAll(pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PublisherResponse> getById(@PathVariable String id) {
        return ApiResponse.<PublisherResponse>builder()
                .result(publisherService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PublisherResponse> update(@PathVariable String id, @RequestBody @Valid PublisherRequest request) {
        return ApiResponse.<PublisherResponse>builder()
                .result(publisherService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> delete(@PathVariable String id) {
        publisherService.delete(id);
        return ApiResponse.<String>builder()
                .result("Publisher has been deleted")
                .build();
    }
}