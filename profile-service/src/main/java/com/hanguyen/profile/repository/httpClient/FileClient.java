package com.hanguyen.profile.repository.httpClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.hanguyen.profile.configuration.AuthenticationRequestInterceptor;
import com.hanguyen.profile.dto.response.UploadResponse;

@FeignClient(
        name = "file-client",
        configuration = {AuthenticationRequestInterceptor.class})
public interface FileClient {
    @PostMapping(
            value = "/file/upload/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    UploadResponse uploadImage(@RequestParam("file") MultipartFile file);
}
