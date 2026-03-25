package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.CICDConfigEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GithubWorkflowService Tests")
class GithubWorkflowServiceTest {

    private GithubWorkflowService workflowService;

    @BeforeEach
    void setUp() {
        workflowService = new GithubWorkflowService();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private CICDConfigEntity buildConfig(
            String projectType,
            String buildTool,
            String branch,
            String runtimeVersion,
            boolean dockerEnabled,
            boolean cdEnabled,
            String deployHookUrl
    ) {
        CICDConfigEntity config = new CICDConfigEntity();
        config.setProjectType(projectType);
        config.setBuildTool(buildTool);
        config.setBranchName(branch);
        config.setRuntimeVersion(runtimeVersion);
        config.setDockerEnabled(dockerEnabled);
        config.setCdEnabled(cdEnabled);
        config.setDeployHookUrl(deployHookUrl);
        return config;
    }

    // ─── Spring Boot (Maven) ─────────────────────────────────────────────────

    @Test
    @DisplayName("Spring Boot + Maven: should generate valid CI workflow")
    void generateWorkflow_springBootMaven_shouldContainMvnTest() {
        CICDConfigEntity config = buildConfig(
                "SPRING_BOOT", "MAVEN", "main", "17", false, false, null);

        String result = workflowService.generateWorkflow(config);

        assertThat(result).contains("name: CI Pipeline");
        assertThat(result).contains("branches: [main]");
        assertThat(result).contains("java-version: '17'");
        assertThat(result).contains("mvn test");
        assertThat(result).doesNotContain("gradle");
        assertThat(result).doesNotContain("docker build");
        assertThat(result).doesNotContain("curl -X POST");
    }

    // ─── Spring Boot (Gradle) ────────────────────────────────────────────────

    @Test
    @DisplayName("Spring Boot + Gradle: should use gradlew test command")
    void generateWorkflow_springBootGradle_shouldContainGradleTest() {
        CICDConfigEntity config = buildConfig(
                "SPRING_BOOT", "GRADLE", "develop", "21", false, false, null);

        String result = workflowService.generateWorkflow(config);

        assertThat(result).contains("./gradlew test");
        assertThat(result).contains("branches: [develop]");
        assertThat(result).contains("java-version: '21'");
    }

    // ─── Spring Boot + Docker ────────────────────────────────────────────────

    @Test
    @DisplayName("Spring Boot + Docker enabled: should include docker build step")
    void generateWorkflow_springBootWithDocker_shouldContainDockerBuild() {
        CICDConfigEntity config = buildConfig(
                "SPRING_BOOT", "MAVEN", "main", "17", true, false, null);

        String result = workflowService.generateWorkflow(config);

        assertThat(result).contains("docker build -t app:latest .");
    }

    // ─── Spring Boot + CD ────────────────────────────────────────────────────

    @Test
    @DisplayName("Spring Boot + CD enabled: should include deploy hook curl step")
    void generateWorkflow_springBootWithCd_shouldContainDeployHook() {
        CICDConfigEntity config = buildConfig(
                "SPRING_BOOT", "MAVEN", "main", "17", false, true,
                "https://hooks.example.com/deploy");

        String result = workflowService.generateWorkflow(config);

        assertThat(result).contains("curl -X POST https://hooks.example.com/deploy");
        assertThat(result).contains("if: success()");
    }

    // ─── Node / npm ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("NODE + npm: should use npm install and npm run build")
    void generateWorkflow_nodeNpm_shouldUseNpmCommands() {
        CICDConfigEntity config = buildConfig(
                "NODE", "NPM", "main", "18", false, false, null);

        String result = workflowService.generateWorkflow(config);

        assertThat(result).contains("node-version: '18'");
        assertThat(result).contains("npm install");
        assertThat(result).contains("npm run build");
        assertThat(result).doesNotContain("yarn");
    }

    // ─── React / yarn ────────────────────────────────────────────────────────

    @Test
    @DisplayName("REACT + Yarn: should use yarn install and yarn build")
    void generateWorkflow_reactYarn_shouldUseYarnCommands() {
        CICDConfigEntity config = buildConfig(
                "REACT", "YARN", "main", "20", false, false, null);

        String result = workflowService.generateWorkflow(config);

        assertThat(result).contains("yarn install");
        assertThat(result).contains("yarn build");
    }

    // ─── Python ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PYTHON: should generate pytest workflow")
    void generateWorkflow_python_shouldContainPytest() {
        CICDConfigEntity config = buildConfig(
                "PYTHON", "PIP", "main", "3.11", false, false, null);

        String result = workflowService.generateWorkflow(config);

        assertThat(result).contains("python-version: '3.11'");
        assertThat(result).contains("pip install -r requirements.txt");
        assertThat(result).contains("pytest");
    }

    // ─── Unsupported project type ─────────────────────────────────────────────

    @Test
    @DisplayName("Unsupported project type: should throw IllegalArgumentException")
    void generateWorkflow_unsupportedType_shouldThrow() {
        CICDConfigEntity config = buildConfig(
                "RUBY", "BUNDLER", "main", "3.2", false, false, null);

        assertThatThrownBy(() -> workflowService.generateWorkflow(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported project type");
    }

    // ─── CD disabled ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("CD disabled: should NOT include deploy hook step")
    void generateWorkflow_cdDisabled_shouldNotContainHook() {
        CICDConfigEntity config = buildConfig(
                "NODE", "NPM", "main", "18", false, false, null);

        String result = workflowService.generateWorkflow(config);

        assertThat(result).doesNotContain("Trigger Hosting Build");
        assertThat(result).doesNotContain("curl -X POST");
    }

    // ─── Branch name propagation ──────────────────────────────────────────────

    @Test
    @DisplayName("Branch name: should be correctly placed in the workflow")
    void generateWorkflow_customBranch_shouldAppearInOutput() {
        CICDConfigEntity config = buildConfig(
                "PYTHON", "PIP", "release/v2.0", "3.10", false, false, null);

        String result = workflowService.generateWorkflow(config);

        assertThat(result).contains("branches: [release/v2.0]");
    }
}