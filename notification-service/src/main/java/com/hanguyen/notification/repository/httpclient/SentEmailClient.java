package com.hanguyen.notification.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.hanguyen.notification.configuration.BrevoRequestInterceptor;
import com.hanguyen.notification.dto.request.EmailRequest;
import com.hanguyen.notification.dto.response.EmailResponse;

@FeignClient(
        name = "sent-email-client",
        url = "${brevo.mcp.api-url}",
        configuration = {BrevoRequestInterceptor.class})
public interface SentEmailClient {
    @PostMapping(value = "/v3/smtp/email", produces = MediaType.APPLICATION_JSON_VALUE)
    EmailResponse sentEmail(@RequestBody EmailRequest body);
}
