package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import org.springframework.stereotype.Service;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.CICDConfigEntity;

@Service
public class GithubWorkflowService {

    public String generateWorkflow(CICDConfigEntity config) {

        String ci = switch (config.getProjectType()) {
            case "SPRING_BOOT" -> springBootWorkflow(config);
            case "NODE", "REACT" -> nodeWorkflow(config);
            case "PYTHON" -> pythonWorkflow(config);
            default -> throw new IllegalArgumentException("Unsupported project type");
        };

        if (!config.isCdEnabled()) {
            return ci;
        }

        return ci + buildHookStep(config.getDeployHookUrl());
    }

    // ================= SPRING BOOT =================

    private String springBootWorkflow(CICDConfigEntity c) {

        String testCmd = c.getBuildTool().equals("GRADLE")
                ? """
                  if [ -d "src/test" ]; then
                    ./gradlew test
                  else
                    echo "No tests found, skipping tests"
                    ./gradlew build -x test
                  fi
                  """
                : """
                  if [ -d "src/test" ]; then
                    mvn test
                  else
                    echo "No tests found, skipping tests"
                    mvn package -DskipTests
                  fi
                  """;

        String dockerStep = c.isDockerEnabled() ? dockerSteps() : "";

        return """
        name: CI Pipeline

        on:
          push:
            branches: [%s]
          pull_request:

        jobs:
          build:
            runs-on: ubuntu-latest

            steps:
              - uses: actions/checkout@v4

              - name: Setup Java
                uses: actions/setup-java@v4
                with:
                  java-version: '%s'
                  distribution: 'temurin'

              - name: Build & Test
                run: |
        %s
        %s
        """.formatted(
                c.getBranchName(),
                c.getRuntimeVersion(),
                indent(testCmd),
                dockerStep
        );
    }

    // ================= NODE / REACT =================

    private String nodeWorkflow(CICDConfigEntity c) {

        String installCmd =
                c.getBuildTool().equals("YARN") ? "yarn install" : "npm install";

        String testCmd = """
            if npm run | grep -q "test"; then
              npm test -- --watch=false
            else
              echo "No test script found, skipping tests"
            fi
            """;

        String buildCmd =
                c.getBuildTool().equals("YARN") ? "yarn build" : "npm run build";

        return """
        name: CI Pipeline

        on:
          push:
            branches: [%s]
          pull_request:

        jobs:
          build:
            runs-on: ubuntu-latest

            steps:
              - uses: actions/checkout@v4

              - name: Setup Node
                uses: actions/setup-node@v4
                with:
                  node-version: '%s'

              - name: Install Dependencies
                run: %s

              - name: Run Tests
                run: |
        %s

              - name: Build App
                run: %s
        """.formatted(
                c.getBranchName(),
                c.getRuntimeVersion(),
                installCmd,
                indent(testCmd),
                buildCmd
        );
    }

    // ================= PYTHON =================

    private String pythonWorkflow(CICDConfigEntity c) {

        return """
        name: CI Pipeline

        on:
          push:
            branches: [%s]
          pull_request:

        jobs:
          build:
            runs-on: ubuntu-latest

            steps:
              - uses: actions/checkout@v4

              - name: Setup Python
                uses: actions/setup-python@v5
                with:
                  python-version: '%s'

              - name: Install dependencies
                run: pip install -r requirements.txt || true

              - name: Run Tests
                run: |
                  if [ -d "tests" ]; then
                    pytest
                  else
                    echo "No tests found, skipping tests"
                  fi
        """.formatted(
                c.getBranchName(),
                c.getRuntimeVersion()
        );
    }

    // ================= DOCKER =================

    private String dockerSteps() {
        return """
              - name: Build Docker Image
                run: docker build -t app:latest .
        """;
    }

    // ================= CD BUILD HOOK =================

    private String buildHookStep(String hookUrl) {
        return """
              - name: Trigger Hosting Build
                if: success()
                run: curl -X POST %s
        """.formatted(hookUrl);
    }

    // ================= UTIL =================

    private String indent(String script) {
        return script.replaceAll("(?m)^", "                ");
    }
}
