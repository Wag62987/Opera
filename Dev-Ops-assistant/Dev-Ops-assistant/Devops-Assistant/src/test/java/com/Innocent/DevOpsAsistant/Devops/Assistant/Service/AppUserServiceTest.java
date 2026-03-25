package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.UserDTO;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppUserService Tests")
class AppUserServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @InjectMocks
    private AppUserService appUserService;

    private AppUser mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new AppUser();
        mockUser.setUsername("john_doe");
        mockUser.setName("John Doe");
        mockUser.setEmail("john@example.com");
    }

    // ─── Save ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Save: should persist and return the user")
    void save_shouldReturnSavedUser() {
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        AppUser result = appUserService.Save(mockUser);

        assertThat(result).isEqualTo(mockUser);
        verify(userRepository, times(1)).save(mockUser);
    }

    // ─── FindById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("FindById: should return user when GitHub ID exists")
    void findById_shouldReturnUser_whenFound() {
        when(userRepository.findByGithubId("gh_123")).thenReturn(Optional.of(mockUser));

        Optional<AppUser> result = appUserService.FindById("gh_123");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john_doe");
    }

    @Test
    @DisplayName("FindById: should return empty when GitHub ID not found")
    void findById_shouldReturnEmpty_whenNotFound() {
        when(userRepository.findByGithubId("unknown")).thenReturn(Optional.empty());

        Optional<AppUser> result = appUserService.FindById("unknown");

        assertThat(result).isEmpty();
    }

    // ─── FindByEmail ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("FindByEmail: should return user when email exists")
    void findByEmail_shouldReturnUser_whenFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(mockUser));

        Optional<AppUser> result = appUserService.FindByEmail("john@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("FindByEmail: should return empty when email not found")
    void findByEmail_shouldReturnEmpty_whenNotFound() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        Optional<AppUser> result = appUserService.FindByEmail("nobody@example.com");

        assertThat(result).isEmpty();
    }

    // ─── GetUserInfo ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("GetUserInfo: should return populated UserDTO when user is present")
    void getUserInfo_shouldReturnDto_whenUserPresent() {
        UserDTO dto = appUserService.GetUserInfo(Optional.of(mockUser));

        assertThat(dto).isNotNull();
        assertThat(dto.getUsername()).isEqualTo("john_doe");
        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("GetUserInfo: should return null when Optional is empty")
    void getUserInfo_shouldReturnNull_whenUserAbsent() {
        UserDTO dto = appUserService.GetUserInfo(Optional.empty());

        assertThat(dto).isNull();
    }
}