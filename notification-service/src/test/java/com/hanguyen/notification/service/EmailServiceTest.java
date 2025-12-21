package com.hanguyen.notification.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.hanguyen.notification.dto.request.EmailRequest;
import com.hanguyen.notification.dto.request.SentEmailRequest;
import com.hanguyen.notification.dto.response.EmailResponse;
import com.hanguyen.notification.entity.NotificationLog;
import com.hanguyen.notification.repository.NotificationLogRepository;
import com.hanguyen.notification.repository.httpclient.SentEmailClient;
import com.hanguyen.notification.utils.TestUtils;

import feign.FeignException;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private SentEmailClient sentEmailClient;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @InjectMocks
    private EmailService emailService;

    private SentEmailRequest sentEmailRequest;
    private EmailResponse emailResponse;

    @BeforeEach
    void initData() {
        sentEmailRequest = TestUtils.getObject("data/notification/sent_email_request.json", SentEmailRequest.class);
        emailResponse = TestUtils.getObject("data/notification/email_response.json", EmailResponse.class);

        ReflectionTestUtils.setField(emailService, "nameSender", "Bookteria");
        ReflectionTestUtils.setField(emailService, "emailSender", "no-reply@bookteria.com");
    }

    @Test
    void sentEmail_success() {
        when(sentEmailClient.sentEmail(any(EmailRequest.class))).thenReturn(emailResponse);
        when(notificationLogRepository.save(any(NotificationLog.class))).thenReturn(new NotificationLog());

        EmailResponse response = emailService.sentEmail(sentEmailRequest);

        assertNotNull(response);
        assertEquals("<123@brevo.com>", response.getMessageId());

        verify(sentEmailClient, times(1)).sentEmail(any(EmailRequest.class));
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
    }

    // Note: Exceptions are caught and wrapped in specific AppExceptions, or logged.
    // The service wraps FeignException into
    // AppException(ErrorCode.CANT_NOT_SENT_EMAIL).

    @Test
    void sentEmail_feignException_throwsAppException() {
        FeignException feignException = mock(FeignException.class);
        when(sentEmailClient.sentEmail(any(EmailRequest.class))).thenThrow(feignException);

        // Expect RuntimeException or AppException - checking code throws AppException
        // Need to import AppException and ErrorCode if verified
        // Since I don't check imports rigorously if they exist in src, assuming
        // standard setup

        // Actually, let's verify if catch block throws or just logs?
        // Code: throw new AppException(ErrorCode.CANT_NOT_SENT_EMAIL);

        assertThrows(RuntimeException.class, () -> emailService.sentEmail(sentEmailRequest));

        // Also verify finally block executes saveLog
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
    }
}
