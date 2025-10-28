package com.devteria.identity.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.devteria.identity.constant.PredefinedRole;
import com.devteria.identity.constant.TypeEvent;
import com.devteria.identity.dto.event.UserEvent;
import com.devteria.identity.dto.request.ProfileCreationRequest;
import com.devteria.identity.dto.request.ProfileUpdateRequest;
import com.devteria.identity.dto.request.UserCreationRequest;
import com.devteria.identity.dto.request.UserUpdateRequest;
import com.devteria.identity.dto.response.UserProfileResponse;
import com.devteria.identity.dto.response.UserResponse;
import com.devteria.identity.entity.Role;
import com.devteria.identity.entity.User;
import com.devteria.identity.exception.AppException;
import com.devteria.identity.exception.ErrorCode;
import com.devteria.identity.mapper.UserMapper;
import com.devteria.identity.repository.RoleRepository;
import com.devteria.identity.repository.UserRepository;
import com.devteria.identity.repository.httpclient.ProfileClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    ProfileClient profileClient;
    UserEventProducerService userEventProducerService;

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
                log.error("Failed to send user creation event for user ID {}: {}", user.getId(), e.getMessage(), e);
            }
        } else {
            log.warn("Profile response or email is null for user ID {}. Skipping Kafka event.", user.getId());
        }

        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setProfileResponse(profileResponse);
        return userResponse;
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository
                .findByUsernameAndIsActiveTrue(name)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserProfileResponse profileResponse = null;
        try {
            profileResponse = profileClient.getProfileByUserId(user.getId());
        } catch (Exception e) {
            log.warn("Unable to fetch profile for user {}: {}", user.getId(), e.getMessage());
        }

        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setProfileResponse(profileResponse);
        return userResponse;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        userMapper.updateUser(user, request);
        if (StringUtils.hasText(request.getPassword())) {
            try {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            } catch (IllegalArgumentException e) {
                log.error("Password encoding failed for user {}: {}", userId, e.getMessage());

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

        UserProfileResponse profileResponse = null;
        try {
            ProfileUpdateRequest profileUpdateRequest = ProfileUpdateRequest.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .dob(request.getDob())
                    .city(request.getCity())
                    .email(request.getEmail())
                    .build();
            profileResponse = profileClient.updateProfileByUserId(userId, profileUpdateRequest);
            log.info("Profile update called for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to update profile for user {}: {}", userId, e.getMessage());
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
                userEventProducerService.sendUserCreationEvent(event);
            } catch (Exception e) {
                log.error("Failed to send user creation event for user ID {}: {}", user.getId(), e.getMessage(), e);
            }
        } else {
            log.warn("Profile response or email is null for user ID {}. Skipping Kafka event.", user.getId());
        }

        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setProfileResponse(profileResponse);

        return userResponse;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserProfileResponse profileResponse = null;
        try {
            profileResponse = profileClient.getProfileByUserId(userId);
        } catch (Exception e) {
            log.warn("Failed to fetch profile for user {} during deletion. Event will lack email.", userId, e);
        }

        user.setActive(false);
        userRepository.save(user);

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

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        var users = userRepository.findAll();
        if (users.isEmpty()) {
            return List.of();
        }

        var userIds = users.stream().map(User::getId).toList();

        List<UserProfileResponse> profiles = List.of();
        try {
            profiles = profileClient.getProfilesByUserIds(userIds);
        } catch (Exception e) {
            log.warn("Unable to fetch bulk profiles: {}", e.getMessage());
        }

        Map<String, UserProfileResponse> profileMap =
                profiles.stream().collect(Collectors.toMap(UserProfileResponse::getUserId, p -> p, (p1, p2) -> p1));

        return users.stream()
                .map(user -> {
                    UserResponse userResponse = userMapper.toUserResponse(user);
                    userResponse.setProfileResponse(profileMap.get(user.getId()));
                    return userResponse;
                })
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        UserProfileResponse profileResponse = null;
        try {
            profileResponse = profileClient.getProfileByUserId(user.getId());
        } catch (Exception e) {
            log.warn("Unable to fetch profile for user {}: {}", user.getId(), e.getMessage());
        }

        UserResponse userResponse = userMapper.toUserResponse(user);
        userResponse.setProfileResponse(profileResponse);
        return userResponse;
    }
}
