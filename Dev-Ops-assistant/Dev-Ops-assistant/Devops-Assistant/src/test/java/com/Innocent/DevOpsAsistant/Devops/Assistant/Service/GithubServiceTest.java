package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.GitRepo;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Exception.UserNotFound;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.GitRepoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GithubService Tests")
class GithubServiceTest {

    @Mock private AppUserService appUserService;
    @Mock private GitRepoRepository gitRepoRepository;
    @Mock private WebClient githubClient;

    @Mock private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private GithubService githubService;

    private AppUser mockUser;
    private GitRepo mockGitRepo;
    private GitRepoEntity mockRepoEntity;

    @BeforeEach
    void setUp() {
        mockUser = new AppUser();
        mockUser.setUsername("john_doe");
        mockUser.setGithub_token("ghp_test_token");

        mockGitRepo = GitRepo.builder()
                .id("1001")
                .name("my-repo")
                .htmlUrl("https://github.com/john_doe/my-repo")
                .description("A test repo")
                .langauage("Java")
                .build();

        mockRepoEntity = new GitRepoEntity();
        mockRepoEntity.setGithubRepoId("1001");
        mockRepoEntity.setRepoName("my-repo");
        mockRepoEntity.setRepoUrl("https://github.com/john_doe/my-repo");
        mockRepoEntity.setAppUser(mockUser);
    }

    // ─── getUserRepos ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getUserRepos: should return list when user exists")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void getUserRepos_shouldReturnRepos_whenUserExists() throws UserNotFound {

        when(appUserService.FindById("gh_123")).thenReturn(Optional.of(mockUser));

        when(githubClient.get())
                .thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);

       when(requestHeadersUriSpec.uri(anyString()))
        .thenAnswer(inv -> requestHeadersSpec);

when(requestHeadersSpec.headers(any()))
        .thenAnswer(inv -> requestHeadersSpec);

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.bodyToFlux(GitRepo.class))
                .thenReturn(Flux.just(mockGitRepo));

        List<GitRepo> result = githubService.getUserRepos("gh_123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("my-repo");
    }

    @Test
    @DisplayName("getUserRepos: should throw UserNotFound when user does not exist")
    void getUserRepos_shouldThrowUserNotFound_whenUserMissing() {
        when(appUserService.FindById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> githubService.getUserRepos("ghost"))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining("User does not exist");
    }

    // ─── importRepo ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("importRepo: should save and return GitRepoEntity when not already imported")
    void importRepo_shouldSaveRepo_whenNotAlreadyImported() {
        when(appUserService.FindById("gh_123")).thenReturn(Optional.of(mockUser));

        // ✅ FIXED (no unnecessary stubbing error)
        when(gitRepoRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GitRepoEntity result = githubService.importRepo("gh_123", mockGitRepo);

        assertThat(result).isNotNull();
        assertThat(result.getRepoName()).isEqualTo("my-repo");

        verify(gitRepoRepository).save(any(GitRepoEntity.class));
    }

    @Test
    @DisplayName("importRepo: should throw RuntimeException when repo already imported")
    void importRepo_shouldThrow_whenRepoAlreadyExists() {
        when(appUserService.FindById("gh_123")).thenReturn(Optional.of(mockUser));
        when(gitRepoRepository.existsByGithubRepoId("1001")).thenReturn(true);

        assertThatThrownBy(() -> githubService.importRepo("gh_123", mockGitRepo))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Repository already imported");

        verify(gitRepoRepository, never()).save(any());
    }

    @Test
    @DisplayName("importRepo: should throw RuntimeException when user not found")
    void importRepo_shouldThrow_whenUserNotFound() {
        when(appUserService.FindById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> githubService.importRepo("unknown", mockGitRepo))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    // ─── getImportedRepos ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getImportedRepos: should return user's repos list")
    void getImportedRepos_shouldReturnRepos() {
        mockUser.setRepos(List.of(mockRepoEntity));
        when(appUserService.FindById("gh_123")).thenReturn(Optional.of(mockUser));

        List<GitRepoEntity> result = githubService.getImportedRepos("gh_123");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRepoName()).isEqualTo("my-repo");
    }

    @Test
    @DisplayName("getImportedRepos: should throw when user not found")
    void getImportedRepos_shouldThrow_whenUserNotFound() {
        when(appUserService.FindById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> githubService.getImportedRepos("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    // ─── getRepoById ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getRepoById: should return repo when found")
    void getRepoById_shouldReturnRepo_whenFound() {
        when(gitRepoRepository.findByGithubRepoId("1L")).thenReturn(Optional.of(mockRepoEntity));

        GitRepoEntity result = githubService.getRepoById("1L");

        assertThat(result).isNotNull();
        assertThat(result.getRepoName()).isEqualTo("my-repo");
    }

    @Test
    @DisplayName("getRepoById: should throw RuntimeException when not found")
    void getRepoById_shouldThrow_whenNotFound() {
        when(gitRepoRepository.findByGithubRepoId("99L")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> githubService.getRepoById("99L"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Repository not found");
    }

    // ─── DeleteRepo ──────────────────────────────────────────────
@Test
@DisplayName("DeleteRepo: should delete and return the repo when found")
void deleteRepo_shouldReturnDeletedRepo_whenExists() {
    when(gitRepoRepository.findByGithubRepoId("1001")).thenReturn(Optional.of(mockRepoEntity));

    GitRepoEntity deleted = githubService.DeleteRepo("1001");

    assertThat(deleted).isNotNull();
    assertThat(deleted.getRepoName()).isEqualTo("my-repo");
    verify(gitRepoRepository).delete(mockRepoEntity);
}

@Test
@DisplayName("DeleteRepo: should throw RuntimeException when repo not found")
void deleteRepo_shouldThrow_whenRepoMissing() {
    when(gitRepoRepository.findByGithubRepoId("9999")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> githubService.DeleteRepo("9999"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Repository not found");

    verify(gitRepoRepository, never()).delete(any());
}

// ─── DeleteAllRepo ─────────────────────────────────────────
@Test
@DisplayName("DeleteAllRepo: should delete all user's repos")
void deleteAllRepo_shouldReturnTrue() {
    mockUser.setRepos(List.of(mockRepoEntity));
    
    Boolean result = githubService.DeleteAllRepo(mockUser);

    assertThat(result).isTrue();
    verify(gitRepoRepository).delete(mockRepoEntity);
}
}