package com.hanguyen.notification.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.hanguyen.notification.dto.ApiResponse;
import com.hanguyen.notification.dto.request.SentEmailRequest;
import com.hanguyen.notification.dto.response.EmailResponse;
import com.hanguyen.notification.service.EmailService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailController {

    EmailService emailService;

    @PostMapping("/email/sent")
    ApiResponse<EmailResponse> sentEmail(@RequestBody SentEmailRequest request) {
        return ApiResponse.<EmailResponse>builder()
                .result(emailService.sentEmail(request))
                .build();
    }
}
