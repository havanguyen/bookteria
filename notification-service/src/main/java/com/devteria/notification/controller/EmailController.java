package com.devteria.notification.controller;

import com.devteria.notification.dto.ApiResponse;
import com.devteria.notification.dto.request.EmailRequest;
import com.devteria.notification.dto.request.SentEmailRequest;
import com.devteria.notification.dto.response.EmailResponse;
import com.devteria.notification.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class EmailController {

    EmailService emailService;

    @PostMapping("/email/sent")
    ApiResponse<EmailResponse> sentEmail(@RequestBody SentEmailRequest request){
        return ApiResponse.<EmailResponse>builder()
                .result(emailService.sentEmail(request))
                .build();
    }
}
