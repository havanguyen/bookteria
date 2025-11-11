package com.hanguyen.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.hanguyen.notification.dto.request.EmailRequest;
import com.hanguyen.notification.dto.request.Sender;
import com.hanguyen.notification.dto.request.SentEmailRequest;
import com.hanguyen.notification.dto.response.EmailResponse;
import com.hanguyen.notification.exception.AppException;
import com.hanguyen.notification.exception.ErrorCode;
import com.hanguyen.notification.repository.httpclient.SentEmailClient;

import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    SentEmailClient sentEmailClient;

    @NonFinal
    @Value("${brevo.mcp.name-sender}")
    String nameSender;

    @NonFinal
    @Value("${brevo.mcp.email-sender}")
    String emailSender;

    public EmailResponse sentEmail(SentEmailRequest request) {

        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder().name(nameSender).email(emailSender).build())
                .to(request.getRecipients())
                .htmlContent(request.getHtmlContent())
                .subject(request.getSubject())
                .build();
        log.info(emailRequest.toString());
        try {
            return sentEmailClient.sentEmail(emailRequest);
        } catch (FeignException e) {
            log.error("FeignException when calling Brevo. Status: {}. Body: {}", e.status(), e.contentUTF8(), e);
            throw new AppException(ErrorCode.CANT_NOT_SENT_EMAIL);
        }
    }
}
