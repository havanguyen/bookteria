package com.hanguyen.identity.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.hanguyen.identity.constant.TypeEvent;
import com.hanguyen.identity.dto.event.UserEvent;
import com.hanguyen.identity.dto.request.*;
import com.hanguyen.identity.dto.response.*;
import com.hanguyen.identity.entity.Permission;
import com.hanguyen.identity.entity.Role;
import com.hanguyen.identity.entity.User;
import com.hanguyen.identity.exception.AppException;
import com.hanguyen.identity.exception.ErrorCode;
import com.hanguyen.identity.repository.InvalidatedTokenRepository;
import com.hanguyen.identity.repository.RoleRepository;
import com.hanguyen.identity.repository.UserRepository;
import com.hanguyen.identity.repository.httpclient.OutboundIdentityClient;
import com.hanguyen.identity.repository.httpclient.OutboundUserClient;
import com.hanguyen.identity.utils.KeyUtils;
import com.hanguyen.identity.utils.PasswordGenerator;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;
    UserEventProducerService userEventProducerService;
    UserService userService;

    RoleRepository roleRepository;

    KeyUtils keyUtils;

    StringRedisTemplate redisTemplate;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${outbound.identity.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${outbound.identity.client-secret}")
    protected String CLIENT_SECRET;

    @NonFinal
    @Value("${outbound.identity.redirect-uri}")
    protected String REDIRECT_URI;

    @NonFinal
    protected String GRANT_TYPE = "authorization_code";

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse outboundAuthenticate(String code) {
        try {
            ExchangeTokenResponse response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                    .code(code)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .redirectUri(REDIRECT_URI)
                    .grantType(GRANT_TYPE)
                    .build());

            log.info("TOKEN RESPONSE {}", response);
            OutboundUserResponse userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

            log.info("User Info {}", userInfo);

            var user = userRepository.findByUsername(userInfo.getEmail());

            TokenInfo tokenInfo;

            if (user.isEmpty()) {
                log.info("User not found, creating new user for email: {}", userInfo.getEmail());
                String passwordRandom = PasswordGenerator.generateStrongPassword(12);

                try {
                    UserResponse userResponse = userService.createUser(UserCreationRequest.builder()
                            .username(userInfo.getEmail())
                            .email(userInfo.getEmail())
                            .firstName(userInfo.getName())
                            .lastName(userInfo.getGivenName())
                            .typeEvent(TypeEvent.OAUTH2.getEvent())
                            .password(passwordRandom)
                            .dob(LocalDate.of(2000, 1, 1))
                            .build());

                    log.info("User created successfully with ID: {}", userResponse.getId());

                    userEventProducerService.sendUserCreationOAuth2Event(UserEvent.builder()
                            .userId(userResponse.getId())
                            .username(userResponse.getUsername())
                            .email(userResponse.getProfileResponse().getEmail())
                            .firstName(userResponse.getProfileResponse().getFirstName())
                            .typeEvent(TypeEvent.OAUTH2.getEvent())
                            .password(passwordRandom)
                            .build());

                    tokenInfo = generateToken(User.builder()
                            .username(userResponse.getUsername())
                            .roles(userResponse.getRoles().stream()
                                    .map(roleResponse -> Role.builder()
                                            .name(roleResponse.getName())
                                            .permissions(roleResponse.getPermissions().stream()
                                                    .map(permissionResponse -> Permission.builder()
                                                            .name(permissionResponse.getName())
                                                            .description(permissionResponse.getDescription())
                                                            .build())
                                                    .collect(Collectors.toSet()))
                                            .description(roleResponse.getDescription())
                                            .build())
                                    .collect(Collectors.toSet()))
                            .id(userResponse.getId())
                            .build());

                    log.info("Token generated successfully for new user");
                } catch (Exception e) {
                    log.error(
                            "Error creating user or generating token for {}: {}",
                            userInfo.getEmail(),
                            e.getMessage(),
                            e);
                    throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
                }
            } else {
                log.info(
                        "User found, generating token for existing user: {}",
                        user.get().getUsername());
                User user1 = user.get();
                tokenInfo = generateToken(user1);
            }

            return AuthenticationResponse.builder()
                    .token(tokenInfo.token)
                    .expiryTime(tokenInfo.expiryDate)
                    .build();
        } catch (Exception e) {
            log.info("Error has message {}", e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(15);
        var user = userRepository
                .findByUsernameAndIsActiveTrue(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token.token)
                .expiryTime(token.expiryDate)
                .refreshToken(token.refreshToken)
                .build();
    }

    public void logout(LogoutRequest request) {
        redisTemplate.delete(request.getToken());
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException {
        String jwt = redisTemplate.opsForValue().get(request.getToken());
        if (jwt == null) throw new AppException(ErrorCode.UNAUTHENTICATED);

        SignedJWT signedJWT = SignedJWT.parse(jwt);
        String userId = signedJWT.getJWTClaimsSet().getSubject();

        var user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        String incomingRefreshTokenHash = computeHash(request.getRefreshToken());

        if (user.getRefreshToken() == null
                || !user.getRefreshToken().equals(incomingRefreshTokenHash)
                || user.getRefreshTokenExpiryTime().isBefore(java.time.LocalDateTime.now())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        TokenInfo newTokenInfo = generateToken(user);

        redisTemplate.delete(request.getToken());

        return AuthenticationResponse.builder()
                .token(newTokenInfo.token)
                .expiryTime(newTokenInfo.expiryDate)
                .refreshToken(newTokenInfo.refreshToken)
                .build();
    }

    private TokenInfo generateToken(User user) {
        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(new JOSEObjectType("at+jwt"))
                .build();

        Date issueTime = new Date();
        Date expiryTime = new Date(Instant.ofEpochMilli(issueTime.getTime())
                .plus(VALID_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli());

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("bookteria.click")
                .issueTime(issueTime)
                .expirationTime(expiryTime)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new com.nimbusds.jose.crypto.RSASSASigner(keyUtils.getPrivateKey()));
            String jwt = jwsObject.serialize();

            String referenceToken = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(referenceToken, jwt, VALID_DURATION, TimeUnit.SECONDS);

            String refreshToken = UUID.randomUUID().toString();
            String refreshTokenHash = computeHash(refreshToken);

            user.setRefreshToken(refreshTokenHash);
            user.setRefreshTokenExpiryTime(java.time.LocalDateTime.now().plusDays(10));
            userRepository.save(user);

            return new TokenInfo(referenceToken, expiryTime, refreshToken);
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private String computeHash(String input) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(encodedhash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public record TokenInfo(String token, Date expiryDate, String refreshToken) {}

    private void verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new RSASSAVerifier(keyUtils.getPublicKey());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });

        return stringJoiner.toString();
    }
}
