package com.devteria.notification.repository.httpclient;

import com.devteria.notification.configuration.BrevoRequestInterceptor;
import com.devteria.notification.dto.request.EmailRequest;
import com.devteria.notification.dto.response.EmailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "sent-email-client" , url = "${brevo.mcp.api-url}" , configuration = {
        BrevoRequestInterceptor.class
})
public interface SentEmailClient {
    @PostMapping(value = "/v3/smtp/email" , produces = MediaType.APPLICATION_JSON_VALUE)
    EmailResponse sentEmail(@RequestBody EmailRequest body);
}
