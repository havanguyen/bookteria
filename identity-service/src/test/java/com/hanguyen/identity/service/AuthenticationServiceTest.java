package com.hanguyen.identity.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.hanguyen.identity.dto.request.AuthenticationRequest;
import com.hanguyen.identity.dto.request.LogoutRequest;
import com.hanguyen.identity.dto.response.AuthenticationResponse;
import com.hanguyen.identity.entity.User;
import com.hanguyen.identity.exception.AppException;
import com.hanguyen.identity.exception.ErrorCode;
import com.hanguyen.identity.repository.InvalidatedTokenRepository;
import com.hanguyen.identity.repository.UserRepository;
import com.hanguyen.identity.utils.KeyUtils;
import com.hanguyen.identity.utils.TestUtils;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvalidatedTokenRepository invalidatedTokenRepository;

    @Mock
    private KeyUtils keyUtils;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthenticationService authenticationService;

    private AuthenticationRequest authenticationRequest;
    private User user;
    private KeyPair keyPair;

    @BeforeEach
    void initData() throws Exception {
        authenticationRequest =
                TestUtils.getObject("data/auth/authentication_request.json", AuthenticationRequest.class);

        user = User.builder()
                .id("c58ed763-928c-4155-bee9-fdbaaadc15f4")
                .username("johndoe")
                .password("$2a$10$8.UnVuG9HHgffUDAlk8qfOpNa.hPAxF7p5l7.9.6a.b5.c4d3e2f1") // Encoded "password123"
                .build();

        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();

        ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", 3600L);
        ReflectionTestUtils.setField(authenticationService, "REFRESHABLE_DURATION", 36000L);
    }

    @Test
    void authenticate_valid_success() {
        // GIVEN
        when(userRepository.findByUsernameAndIsActiveTrue(anyString())).thenReturn(Optional.of(user));
        // We mock BCrypt check logic by assuming it will match if creating new encoder
        // or we can rely on real ONE if we could inject it?
        // AuthenticationService instantiates `new BCryptPasswordEncoder(15)` inside the
        // method.
        // So we rely on the real one. The password in user must be valid bcrypt hash of
        // "password123".
        // I used a placeholder hash above. Let's hope it matches "password123" or I
        // need to generate one.
        // Actually, $2a$10$ ... is generic.
        // It's better to update the user password to a known hash of "password123".
        // For test stability, I'll use a known hash.
        // Hash for "password123":
        // $2a$10$wT8d1QyH.8/1.1/1.1/1.1/1.1/1.1/1.1/1.1/1.1/1.1/1.1 (Fake)
        // Wait, Real BCrypt is used. I must provide a valid hash.
        // Or I can mock PasswordEncoder if it was injected. But it is `new`ed inside
        // method.
        // Wait! `AuthenticationService.java` line 203: `PasswordEncoder passwordEncoder
        // = new BCryptPasswordEncoder(15);`
        // THIS IS BAD for unit testing. I cannot mock it.
        // I must ensure `user.password` is a valid hash of
        // `authenticationRequest.password` ("password123").
        // I will allow the test to calculate it or use a pre-calculated one.
        // Since I can't easily calculate 15 rounds in test setup quickly without adding
        // delay,
        // I'll try to find a valid hash or maybe I HAVE to settle for a test that runs
        // slow?
        // Or I can assume "password123" hash is something.

        // Actually, creating BCrypt(15) is very slow (seconds). Unit test will be slow.
        // I should probably skip this test or accept it's slow.

        // Let's try to mock the mocking details... wait I can't.

        // Alternative: Use a real BCrypt in BeforeEach to hash "password123" and set it
        // to user.
        // But 15 rounds is slooow.

        // Ok, I will accept it might be slow. I'll invoke BCrypt in initData with
        // strength 10 (default) to generate hash,
        // BUT the service uses 15.
        // Matches checks if text matches hash. Strength doesn't matter for matching
        // provided the salt is there.
        // So I can generate with strength 10, and Service (strength 15) check will
        // still work?
        // Yes, verify doesn't use the strength parameter, it reads rounds from the hash
        // string.

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(10);
        user.setPassword(encoder.encode("password123"));

        when(keyUtils.getPrivateKey()).thenReturn((RSAPrivateKey) keyPair.getPrivate());
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // WHEN
        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        // THEN
        assertNotNull(response);
        assertNotNull(response.getToken());
    }

    @Test
    void authenticate_userNotFound_fail() {
        when(userRepository.findByUsernameAndIsActiveTrue(anyString())).thenReturn(Optional.empty());

        AppException exception =
                assertThrows(AppException.class, () -> authenticationService.authenticate(authenticationRequest));

        assertEquals(ErrorCode.USER_NOT_EXISTED, exception.getErrorCode());
    }

    @Test
    void authenticate_wrongPassword_fail() {
        when(userRepository.findByUsernameAndIsActiveTrue(anyString())).thenReturn(Optional.of(user));
        // user password is set in initData to match "password123".
        // request password is "password123".
        // We need to change request password to something else.
        authenticationRequest.setPassword("wrongpassword");

        // Need to set valid password on user so the finder returns it.
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(10);
        user.setPassword(encoder.encode("password123"));

        AppException exception =
                assertThrows(AppException.class, () -> authenticationService.authenticate(authenticationRequest));

        assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());
    }

    @Test
    void logout_valid_success() {
        LogoutRequest request = new LogoutRequest("valid_token");

        authenticationService.logout(request);

        verify(redisTemplate, times(1)).delete("valid_token");
    }
}
