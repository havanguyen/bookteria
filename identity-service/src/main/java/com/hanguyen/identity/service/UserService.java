package com.hanguyen.identity.service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.hanguyen.identity.constant.PredefinedRole;
import com.hanguyen.identity.constant.TypeEvent;
import com.hanguyen.identity.dto.event.UserEvent;
import com.hanguyen.identity.dto.request.ProfileCreationRequest;
import com.hanguyen.identity.dto.request.ProfileUpdateRequest;
import com.hanguyen.identity.dto.request.UserCreationRequest;
import com.hanguyen.identity.dto.request.UserUpdateRequest;
import com.hanguyen.identity.dto.response.UserPageResponse;
import com.hanguyen.identity.dto.response.UserProfileResponse;
import com.hanguyen.identity.dto.response.UserResponse;
import com.hanguyen.identity.entity.Role;
import com.hanguyen.identity.entity.User;
import com.hanguyen.identity.exception.AppException;
import com.hanguyen.identity.exception.ErrorCode;
import com.hanguyen.identity.mapper.UserMapper;
import com.hanguyen.identity.repository.RoleRepository;
import com.hanguyen.identity.repository.UserRepository;
import com.hanguyen.identity.repository.httpclient.ProfileClient;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Data
public class UserService {
    final UserRepository userRepository;
    final RoleRepository roleRepository;
    final UserMapper userMapper;
    final PasswordEncoder passwordEncoder;
    final ProfileClient profileClient;
    final UserEventProducerService userEventProducerService;

    final RedisTemplate<String, Object> stringObjectRedisTemplate;

    String identityKeyPrefix = "identity::";
    String identityListKeyPrefix = "identity_list::";
    String identityListKeySet = "identity_list_keys";

    int minTTL = 1200;
    int maxTTL = 1800;

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsernameAndIsActiveTrue(request.getUsername()))
            throw new AppException(ErrorCode.USER_EXISTED);

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);

        user = userRepository.save(user);

        ProfileCreationRequest profileRequest = userMapper.toProfileCreationRequest(request);

        profileRequest.setUserId(user.getId());

        UserProfileResponse profileResponse = profileClient.createProfile(profileRequest);

        if (profileResponse != null && profileResponse.getEmail() != null) {
            try {
                UserEvent event = UserEvent.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .firstName(profileResponse.getFirstName())
                        .email(profileResponse.getEmail())
                        .typeEvent(TypeEvent.CREATE.getEvent())
                        .build();
                userEventProducerService.sendUserCreationEvent(event);
            } catch (Exception e) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        } else {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setProfileResponse(profileResponse);

        clearUserListCaches();
        return userResponse;
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository
                .findByUsernameAndIsActiveTrue(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String key = identityKeyPrefix + user.getId();

        UserResponse userResponse =
                (UserResponse) stringObjectRedisTemplate.opsForValue().get(key);
        if (userResponse != null) {
            log.info("Cache hit for getInfoUser: {}", user.getId());
            return userResponse;
        }

        log.info("Cache miss for getInfoUser: {}", user.getId());
        UserProfileResponse profileResponse;
        try {
            profileResponse = profileClient.getProfileByUserId(user.getId());
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        userResponse = userMapper.toUserResponse(user);
        userResponse.setProfileResponse(profileResponse);

        int randomTTL = ThreadLocalRandom.current().nextInt(minTTL, maxTTL + 1);
        stringObjectRedisTemplate.opsForValue().set(key, userResponse, Duration.ofSeconds(randomTTL));

        return userResponse;
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        if (StringUtils.hasText(request.getPassword())) {
            try {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }
        if (request.getRoles() != null) {
            var roles = roleRepository.findAllById(request.getRoles());
            user.setRoles(new HashSet<>(roles));
        }

        if (request.getIsActive() != null) {
            user.setActive(request.getIsActive());
        }

        user = userRepository.save(user);

        UserProfileResponse profileResponse;
        try {
            ProfileUpdateRequest profileUpdateRequest = ProfileUpdateRequest.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .dob(request.getDob())
                    .city(request.getCity())
                    .email(request.getEmail())
                    .avatarUrl(request.getAvatarUrl())
                    .build();
            profileResponse = profileClient.updateProfileByUserId(userId, profileUpdateRequest);
            log.info("Profile update called for userId: {}", userId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        if (profileResponse != null && profileResponse.getEmail() != null) {
            try {
                UserEvent event = UserEvent.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .firstName(profileResponse.getFirstName())
                        .email(profileResponse.getEmail())
                        .typeEvent(TypeEvent.UPDATE.getEvent())
                        .build();
                userEventProducerService.sendUserUpdateEvent(event);
            } catch (Exception e) {
                log.error("Failed to send user creation event for user ID {}: {}", user.getId(), e.getMessage(), e);
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        } else {
            log.warn("Profile response or email is null for user ID {}. Skipping Kafka event.", user.getId());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setProfileResponse(profileResponse);

        String key = identityKeyPrefix + userId;
        stringObjectRedisTemplate.delete(key);
        clearUserListCaches();
        log.info("Delete cache with id {}", userId);

        return userResponse;
    }

    public UserResponse updateMyInfo(UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        User user = userRepository
                .findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);

        if (StringUtils.hasText(request.getPassword())) {
            try {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            } catch (IllegalArgumentException e) {
                log.error("Password encoding failed for user {}: {}", user.getId(), e.getMessage());
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }

        user = userRepository.save(user);

        UserProfileResponse profileResponse;
        try {
            ProfileUpdateRequest profileUpdateRequest = ProfileUpdateRequest.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .dob(request.getDob())
                    .city(request.getCity())
                    .email(request.getEmail())
                    .avatarUrl(request.getAvatarUrl())
                    .build();
            profileResponse = profileClient.updateProfileByUserId(user.getId(), profileUpdateRequest);
            log.info("Profile update called by user {} for userId: {}", username, user.getId());
        } catch (Exception e) {
            log.error("Failed to update profile for user {}: {}", user.getId(), e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        if (profileResponse != null && StringUtils.hasText(profileResponse.getEmail())) {
            try {
                UserEvent event = UserEvent.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .firstName(profileResponse.getFirstName())
                        .email(profileResponse.getEmail())
                        .typeEvent(TypeEvent.UPDATE.getEvent())
                        .build();
                userEventProducerService.sendUserUpdateEvent(event);
            } catch (Exception e) {
                log.error("Failed to send user update event for user ID {}: {}", user.getId(), e.getMessage(), e);
            }
        } else {
            log.warn(
                    "Profile response or email is null after update for user ID {}. Skipping Kafka event.",
                    user.getId());
        }

        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setProfileResponse(profileResponse);

        String key = identityKeyPrefix + user.getId();
        stringObjectRedisTemplate.delete(key);
        clearUserListCaches();
        log.info("Delete cache my info {}", user.getId());

        return userResponse;
    }

    public void deleteUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserProfileResponse profileResponse;
        try {
            profileResponse = profileClient.getProfileByUserId(userId);
        } catch (Exception e) {
            log.warn("Failed to fetch profile for user {} during deletion. Event will lack email.", userId, e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        user.setActive(false);
        userRepository.save(user);

        String key = identityKeyPrefix + userId;
        stringObjectRedisTemplate.delete(key);
        clearUserListCaches();
        log.info("Deactivate user {}", userId);

        if (profileResponse != null && StringUtils.hasText(profileResponse.getEmail())) {
            try {
                UserEvent event = UserEvent.builder()
                        .userId(user.getId())
                        .typeEvent(TypeEvent.DELETE.getEvent())
                        .username(user.getUsername())
                        .email(profileResponse.getEmail())
                        .build();
                userEventProducerService.sendUserDeleteEvent(event);
            } catch (Exception e) {
                log.error("Failed to send user deletion event for user ID {}: {}", user.getId(), e.getMessage());
            }
        } else {
            log.warn("Skipping Kafka deletion event for user {} due to missing profile or email.", userId);
        }
    }

    public Page<UserResponse> getUsers(Pageable pageable) {

        String key = identityListKeyPrefix + generateProductCacheKey(pageable);

        UserPageResponse userPageResponse = null;
        try {
            userPageResponse =
                    (UserPageResponse) stringObjectRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Error reading from Redis cache: {}", e.getMessage());
        }
        if (userPageResponse != null) {
            log.info("Cache hit for getUsers list: {}", key);
            Pageable pageRequest =
                    PageRequest.of(userPageResponse.getPageNo(), userPageResponse.getPageSize(), pageable.getSort());
            return new PageImpl<>(userPageResponse.getContent(), pageRequest, userPageResponse.getTotalElements());
        }

        log.info("Cache miss for getUsers list. Fetching from DB");

        Page<User> userPage = userRepository.findAll(pageable);

        if (userPage.isEmpty()) {
            return Page.empty(pageable);
        }

        var userIds = userPage.getContent().stream().map(User::getId).toList();

        List<UserProfileResponse> profiles = List.of();
        try {
            profiles = profileClient.getProfilesByUserIds(userIds);
        } catch (Exception e) {
            log.warn("Unable to fetch bulk profiles: {}", e.getMessage());
        }

        Map<String, UserProfileResponse> profileMap =
                profiles.stream().collect(Collectors.toMap(UserProfileResponse::getUserId, p -> p, (p1, p2) -> p1));

        Page<UserResponse> userResponses = userPage.map(user -> {
            UserResponse userResponse = userMapper.toUserResponse(user);
            userResponse.setProfileResponse(profileMap.get(user.getId()));
            return userResponse;
        });

        UserPageResponse pageToCache = new UserPageResponse(
                userResponses.getContent(),
                userResponses.getNumber(),
                userResponses.getSize(),
                userResponses.getTotalElements(),
                userResponses.getTotalPages(),
                userResponses.isLast());

        int randomTTL = ThreadLocalRandom.current().nextInt(minTTL, maxTTL + 1);

        try {
            stringObjectRedisTemplate.opsForValue().set(key, pageToCache, Duration.ofSeconds(randomTTL));
            stringObjectRedisTemplate.opsForSet().add(identityListKeySet, key);
        } catch (Exception e) {
            log.warn("Error writing to Redis cache: {}", e.getMessage());
        }

        return userResponses;
    }

    public UserResponse getUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String key = identityKeyPrefix + id;

        UserResponse userResponse =
                (UserResponse) stringObjectRedisTemplate.opsForValue().get(key);

        if (userResponse != null) {
            log.info("Cache hit for Info user: {}", id);
            return userResponse;
        }
        log.info("Cache miss for Info user: {}", id);

        UserProfileResponse profileResponse;
        try {
            profileResponse = profileClient.getProfileByUserId(user.getId());
        } catch (Exception e) {
            log.warn("Unable to fetch profile for user {}: {}", user.getId(), e.getMessage());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        userResponse = userMapper.toUserResponse(user);
        userResponse.setProfileResponse(profileResponse);

        int randomTTL = ThreadLocalRandom.current().nextInt(minTTL, maxTTL + 1);
        stringObjectRedisTemplate.opsForValue().set(key, userResponse, Duration.ofSeconds(randomTTL));
        return userResponse;
    }

    private String generateProductCacheKey(Pageable pageable) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder
                .append("page=")
                .append(pageable.getPageNumber())
                .append(":size=")
                .append(pageable.getPageSize());
        if (pageable.getSort().isSorted()) {
            keyBuilder.append(":sort=");
            pageable.getSort().forEach(order -> keyBuilder
                    .append(order.getProperty())
                    .append("-")
                    .append(order.getDirection().name())
                    .append("_"));
        }
        return keyBuilder.toString();
    }

    private void clearUserListCaches() {
        log.info("Clearing all user list caches");
        Set<Object> keys = stringObjectRedisTemplate.opsForSet().members(identityListKeySet);
        if (keys != null && !keys.isEmpty()) {
            keys.add(identityListKeySet);
            Set<String> keyStrings = keys.stream().map(Object::toString).collect(Collectors.toSet());
            stringObjectRedisTemplate.delete(keyStrings);
        }
    }
}
