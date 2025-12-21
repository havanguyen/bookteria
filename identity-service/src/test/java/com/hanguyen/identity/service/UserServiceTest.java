package com.hanguyen.identity.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hanguyen.identity.constant.PredefinedRole;
import com.hanguyen.identity.dto.request.ProfileCreationRequest;
import com.hanguyen.identity.dto.request.UserCreationRequest;
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
import com.hanguyen.identity.utils.TestUtils;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProfileClient profileClient;

    @Mock
    private UserEventProducerService userEventProducerService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private SetOperations<String, Object> setOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private UserService userService;

    private UserCreationRequest userCreationRequest;
    private UserResponse userResponse;
    private UserProfileResponse userProfileResponse;
    private User user;

    @BeforeEach
    void initData() {
        userCreationRequest = TestUtils.getObject("data/user/create_user_request.json", UserCreationRequest.class);
        userResponse = TestUtils.getObject("data/user/user_response.json", UserResponse.class);
        userProfileResponse = TestUtils.getObject("data/user/user_profile_response.json", UserProfileResponse.class);

        user = User.builder()
                .id("c58ed763-928c-4155-bee9-fdbaaadc15f4")
                .username("johndoe")
                .password("encodedPassword")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createUser_validRequest_success() {
        // GIVEN
        when(userRepository.existsByUsernameAndIsActiveTrue(anyString())).thenReturn(false);
        when(userMapper.toUser(any(UserCreationRequest.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findById(anyString()))
                .thenReturn(Optional.of(new Role(PredefinedRole.USER_ROLE, "User role", null)));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toProfileCreationRequest(any(UserCreationRequest.class)))
                .thenReturn(new ProfileCreationRequest());
        when(profileClient.createProfile(any(ProfileCreationRequest.class))).thenReturn(userProfileResponse);
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        // Mock Redis
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members(anyString())).thenReturn(null);

        // WHEN
        UserResponse response = userService.createUser(userCreationRequest);

        // THEN
        assertNotNull(response);
        assertEquals("johndoe", response.getUsername());
        verify(userEventProducerService, times(1)).sendUserCreationEvent(any());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_userExisted_fail() {
        // GIVEN
        when(userRepository.existsByUsernameAndIsActiveTrue(anyString())).thenReturn(true);

        // WHEN
        AppException exception = assertThrows(AppException.class, () -> userService.createUser(userCreationRequest));

        // THEN
        // The service catches the exception and rethrows UNCATEGORIZED_EXCEPTION
        assertEquals(ErrorCode.UNCATEGORIZED_EXCEPTION, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_profileCreationFails_throwsException() {
        // GIVEN
        when(userRepository.existsByUsernameAndIsActiveTrue(anyString())).thenReturn(false);
        when(userMapper.toUser(any(UserCreationRequest.class))).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findById(anyString()))
                .thenReturn(Optional.of(new Role(PredefinedRole.USER_ROLE, "User role", null)));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toProfileCreationRequest(any(UserCreationRequest.class)))
                .thenReturn(new ProfileCreationRequest());

        // Mock profile client returning null
        when(profileClient.createProfile(any(ProfileCreationRequest.class))).thenReturn(null);

        // WHEN & THEN
        assertThrows(AppException.class, () -> userService.createUser(userCreationRequest));
    }

    @Test
    void getMyInfo_valid_success() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("johndoe");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByUsernameAndIsActiveTrue(anyString())).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);
        when(profileClient.getProfileByUserId(anyString())).thenReturn(userProfileResponse);

        // Mock Redis
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        UserResponse response = userService.getMyInfo();

        assertNotNull(response);
        assertEquals("johndoe", response.getUsername());
    }

    @Test
    void getUsers_valid_success() {
        // GIVEN
        Page<User> page = new PageImpl<>(Collections.singletonList(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        // Note: getUsers calls profileClient.getProfilesByUserIds, mock it
        when(profileClient.getProfilesByUserIds(any())).thenReturn(Collections.singletonList(userProfileResponse));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(userResponse);

        // Mock Redis
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        // WHEN
        Page<UserResponse> response = userService.getUsers(PageRequest.of(0, 10));

        // THEN
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }
}
