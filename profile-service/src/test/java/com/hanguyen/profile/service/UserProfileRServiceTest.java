package com.hanguyen.profile.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.hanguyen.profile.dto.request.ProfileCreationRequest;
import com.hanguyen.profile.dto.request.ProfileUpdateRequest;
import com.hanguyen.profile.dto.response.UploadResponse;
import com.hanguyen.profile.dto.response.UserProfileResponse;
import com.hanguyen.profile.entity.UserProfile;
import com.hanguyen.profile.mapper.UserProfileMapper;
import com.hanguyen.profile.repository.UserProfileRepository;
import com.hanguyen.profile.repository.httpClient.FileClient;
import com.hanguyen.profile.utils.TestUtils;

@ExtendWith(MockitoExtension.class)
public class UserProfileRServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private FileClient fileClient;

    @InjectMocks
    private UserProfileService userProfileService;

    private ProfileCreationRequest profileCreationRequest;
    private UserProfileResponse userProfileResponse;
    private UserProfile userProfile;

    @BeforeEach
    void initData() {
        profileCreationRequest =
                TestUtils.getObject("data/profile/create_profile_request.json", ProfileCreationRequest.class);
        userProfileResponse = TestUtils.getObject("data/profile/user_profile_response.json", UserProfileResponse.class);

        userProfile = UserProfile.builder()
                .id(UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3"))
                .userId("c58ed763-928c-4155-bee9-fdbaaadc15f4")
                .firstName("John")
                .lastName("Doe")
                .dob(LocalDate.of(1990, 1, 1))
                .city("New York")
                .email("john.doe@example.com")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProfile_validRequest_success() {
        when(userProfileRepository.findByUserId(anyString())).thenReturn(Optional.empty());
        when(userProfileMapper.toUserProfile(any(ProfileCreationRequest.class))).thenReturn(userProfile);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        when(userProfileMapper.toUserProfileResponse(any(UserProfile.class))).thenReturn(userProfileResponse);

        UserProfileResponse response = userProfileService.createProfile(profileCreationRequest);

        assertNotNull(response);
        assertEquals("c58ed763-928c-4155-bee9-fdbaaadc15f4", response.getUserId());
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void getProfile_validId_success() {
        when(userProfileRepository.findById(any(UUID.class))).thenReturn(Optional.of(userProfile));
        when(userProfileMapper.toUserProfileResponse(any(UserProfile.class))).thenReturn(userProfileResponse);

        UserProfileResponse response =
                userProfileService.getProfile(UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3"));

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
    }

    @Test
    void updateProfileByUserId_existingProfile_success() {
        ProfileUpdateRequest request =
                new ProfileUpdateRequest("Jane", "Doe", LocalDate.now(), "City", "email@test.com", "url");

        when(userProfileRepository.findByUserId(anyString())).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        when(userProfileMapper.toUserProfileResponse(any(UserProfile.class))).thenReturn(userProfileResponse);

        UserProfileResponse response =
                userProfileService.updateProfileByUserId("c58ed763-928c-4155-bee9-fdbaaadc15f4", request);

        assertNotNull(response);
        verify(userProfileMapper, times(1)).updateUserProfile(any(UserProfile.class), any(ProfileUpdateRequest.class));
    }

    @Test
    void updateAvatar_success() {
        // Mock Security Context
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        // Mock Jwt principal
        org.springframework.security.oauth2.jwt.Jwt jwt = mock(org.springframework.security.oauth2.jwt.Jwt.class);
        when(jwt.getClaimAsString("sub")).thenReturn("c58ed763-928c-4155-bee9-fdbaaadc15f4");
        when(authentication.getPrincipal()).thenReturn(jwt);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        MultipartFile file = mock(MultipartFile.class);
        UploadResponse uploadResponse = new UploadResponse("https://new-avatar.com");

        when(fileClient.uploadImage(any())).thenReturn(uploadResponse);
        when(userProfileRepository.findByUserId(anyString())).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        when(userProfileMapper.toUserProfileResponse(any(UserProfile.class))).thenReturn(userProfileResponse);

        UserProfileResponse response = userProfileService.updateAvatar(file);

        assertNotNull(response);
        verify(fileClient, times(1)).uploadImage(any());
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }
}
