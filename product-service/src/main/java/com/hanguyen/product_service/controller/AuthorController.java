package com.hanguyen.product_service.controller;


import com.hanguyen.product_service.dto.request.AuthorRequest;
import com.hanguyen.product_service.dto.response.ApiResponse;
import com.hanguyen.product_service.dto.response.AuthorResponse;
import com.hanguyen.product_service.service.AuthorService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authors")
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class AuthorController {

    AuthorService authorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AuthorResponse> create(@RequestBody @Valid AuthorRequest request) {
        return ApiResponse.<AuthorResponse>builder()
                .result(authorService.createAuthor(request))
                .build();
    }

    @GetMapping
    public ApiResponse<Page<AuthorResponse>> getAll(Pageable pageable) {
        return ApiResponse.<Page<AuthorResponse>>builder()
                .result(authorService.getAll(pageable))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<AuthorResponse> getById(@PathVariable String id) {
        return ApiResponse.<AuthorResponse>builder()
                .result(authorService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AuthorResponse> update(@PathVariable String id, @RequestBody @Valid AuthorRequest request) {
        return ApiResponse.<AuthorResponse>builder()
                .result(authorService.updateAuthor(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> delete(@PathVariable String id) {
        authorService.delete(id);
        return ApiResponse.<String>builder()
                .result("Author has been deleted")
                .build();
    }
}
