# Jenkins Assignment 4 — Declarative CI Pipeline

## Table of Contents

1. [Assignment](#1-assignment)
2. [Assignment Objective](#2-assignment-objective)
3. [Pipeline Overview](#3-pipeline-overview)
4. [Pipeline Stages](#4-pipeline-stages)
   - [Code Checkout](#41-code-checkout)
   - [Parallel Scans](#42-parallel-scans)
   - [Code Stability](#43-code-stability)
   - [Code Quality Analysis](#44-code-quality-analysis)
   - [Code Coverage Analysis](#45-code-coverage-analysis)
   - [Approval Stage](#46-approval-stage)
   - [Publish Artifacts](#47-publish-artifacts)
   - [Notifications](#48-notifications)
5. [Build Parameters](#5-build-parameters)
6. [Reports Generated](#6-reports-generated)
7. [Final Implementation Status](#7-final-implementation-status)

---

# 1. Assignment

Create a **Declarative CI Pipeline for a Java-based project** that contains various stages such as:

- Code checkout
- Run the following stages in parallel:
  - Code stability
  - Code quality analysis
  - Code coverage analysis
- Generate reports for code quality and analysis
- Publish artifacts
- Send Slack and Email notifications
- Provide the user with an option to skip various scans during build execution
- Before publishing artifacts, provide an approval stage where the user can **Approve or Deny** publication
- If publication is approved, publish the artifact and notify the user of the success or failure

---

# 2. Assignment Objective

The main objective of this assignment is to create a **CI pipeline using Jenkins Declarative Pipeline** that automates the process of:

```text
Code Checkout
      ↓
Parallel Code Analysis
      ↓
Generate Reports
      ↓
Approval
      ↓
Publish Artifact
      ↓
Send Notifications
```

The pipeline also provides build parameters that allow users to selectively enable or disable different scans.

This demonstrates important Jenkins CI concepts such as:

- Declarative Pipelines
- Git integration
- Parallel execution
- Build parameters
- JUnit test reporting
- JaCoCo code coverage
- Checkstyle code quality analysis
- Manual approval
- Conditional stages
- Artifact archiving
- Slack notifications
- Email notifications

---

# 3. Pipeline Overview

```text
                    Code Checkout
                         │
                         ↓
                  ┌───────────────┐
                  │ Parallel Scans│
                  └───────────────┘
                    /      |      \
                   /       |       \
                  ↓        ↓        ↓
             Stability  Quality  Coverage
                  \       |       /
                   \      |      /
                    └─────┬─────┘
                          ↓
                       Approval
                      /        \
                     /          \
                Approve          Deny
                   ↓              ↓
            Publish Artifact    Skip
                   ↓
             Notifications
             /            \
          Slack           Email
```

---

# 4. Pipeline Stages

## 4.1 Code Checkout

The pipeline first checks out the Java project from GitHub.

```groovy
stage('Code Checkout') {
    steps {
        git branch: 'master',
            url: 'https://github.com/rahul96saini/spring3hibernate.git'
    }
}
```

### Purpose

This stage downloads the latest source code from the `master` branch of the GitHub repository into the Jenkins workspace.

---

## 4.2 Parallel Scans

The pipeline executes the three analysis stages in parallel.

```groovy
stage('Parallel Scans') {
    parallel {
        ...
    }
}
```

Running these stages in parallel reduces the overall pipeline execution time because Jenkins does not have to wait for one scan to finish before starting another.

The three parallel stages are:

1. Code Stability
2. Code Quality Analysis
3. Code Coverage Analysis

---

## 4.3 Code Stability

The Code Stability stage executes the project's unit tests using Maven.

```groovy
stage('Code Stability') {
    when {
        expression {
            params.RUN_CODE_STABILITY
        }
    }

    steps {
        sh 'mvn test'
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
    }
}
```

### Purpose

The `mvn test` command executes the project's unit tests.

Jenkins then collects the generated JUnit XML reports using:

```groovy
junit 'target/surefire-reports/*.xml'
```

The test results can be viewed from the Jenkins build's **Tests** section.

---

## 4.4 Code Quality Analysis

Checkstyle is used to analyze the source code.

```groovy
stage('Code Quality Analysis') {
    when {
        expression {
            params.RUN_CODE_QUALITY
        }
    }

    steps {
        sh 'mvn checkstyle:checkstyle'
    }

    post {
        always {
            archiveArtifacts artifacts: 'target/checkstyle-result.xml',
                               fingerprint: true
        }
    }
}
```

### Purpose

The Maven Checkstyle plugin analyzes the source code according to configured coding standards.

The generated Checkstyle report is:

```text
target/checkstyle-result.xml
```

The report is archived by Jenkins as a build artifact.

---

## 4.5 Code Coverage Analysis

JaCoCo is used to generate the code coverage report.

```groovy
stage('Code Coverage Analysis') {
    when {
        expression {
            params.RUN_CODE_COVERAGE
        }
    }

    steps {
        sh 'mvn jacoco:report'
    }

    post {
        always {
            jacoco execPattern: 'target/jacoco.exec'
        }
    }
}
```

### Purpose

JaCoCo measures how much of the source code is executed by the tests.

Jenkins publishes the JaCoCo results so that the coverage information can be viewed from the build page under **Coverage Report**.

---

# 4.6 Approval Stage

Before publishing the WAR artifact, Jenkins pauses and asks the user for a decision.

```groovy
stage('Approval') {
    steps {
        script {
            def decision = input(
                message: 'Approve artifact publication?',
                ok: 'Submit',
                parameters: [
                    choice(
                        name: 'APPROVAL',
                        choices: ['Approve', 'Deny'],
                        description: 'Choose whether to publish the artifact'
                    )
                ]
            )

            echo "Publication decision: ${decision}"

            if (decision == 'Approve') {
                publicationApproved = true
            } else {
                publicationApproved = false
            }
        }
    }
}
```

### Approval Options

The user gets two choices:

```text
Approve
Deny
```

### If Approve is selected

The `publicationApproved` variable is set to:

```text
true
```

The pipeline can then proceed to artifact publishing.

### If Deny is selected

The variable is set to:

```text
false
```

The publishing stage is skipped.

This ensures that artifacts cannot be published without manual approval.

---

# 4.7 Publish Artifacts

The WAR file generated by the Maven build is archived by Jenkins only when publication has been approved.

```groovy
stage('Publish Artifacts') {
    when {
        expression {
            publicationApproved
        }
    }

    steps {
        archiveArtifacts artifacts: 'target/*.war',
                           fingerprint: true
    }
}
```

### Purpose

The generated WAR file is stored as a Jenkins build artifact.

The artifact can be accessed from the Jenkins build page under:

```text
Build Artifacts
```

The pipeline uses:

```text
target/*.war
```

to select the generated WAR file.

---

# 4.8 Notifications

The pipeline sends notifications through both **Slack** and **Email** after artifact publication.

## Successful Publication

If the artifact is successfully published, Jenkins sends a Slack message and an email containing:

- Job name
- Build number
- Build status
- Publication status

Example Slack message:

```text
SUCCESS: javaapp-CI #BUILD_NUMBER - Artifact published successfully.
```

## Failed Publication

If artifact publication fails, Jenkins sends a Slack message and an email informing the user that the publication failed and that the Jenkins console should be checked.

The notifications are implemented using:

```groovy
slackSend(...)
```

and:

```groovy
emailext(...)
```

---

# 5. Build Parameters

The pipeline provides three Boolean parameters that allow users to skip individual scans.

### Code Stability

```text
RUN_CODE_STABILITY
```

Controls whether the Code Stability stage runs.

### Code Quality

```text
RUN_CODE_QUALITY
```

Controls whether the Code Quality Analysis stage runs.

### Code Coverage

```text
RUN_CODE_COVERAGE
```

Controls whether the Code Coverage Analysis stage runs.

For example, if the user disables:

```text
RUN_CODE_QUALITY
```

the Code Quality Analysis stage is skipped while the other enabled scans continue.

This provides flexibility during build execution.

---

# 6. Reports Generated

The pipeline generates and publishes the following reports:

| Report | Tool | Jenkins Location |
|---|---|---|
| Unit Test Report | JUnit | **Tests** |
| Code Coverage Report | JaCoCo | **Coverage Report** |
| Code Quality Report | Checkstyle | **Build Artifacts** |

### JUnit

Displays:

- Number of tests
- Passed tests
- Failed tests
- Test execution details

### JaCoCo

Displays coverage information such as:

- Instruction coverage
- Branch coverage
- Complexity
- Line coverage
- Method coverage
- Class coverage

### Checkstyle

The generated XML report is archived as:

```text
checkstyle-result.xml
```

---

# 7. Final Implementation Status

| Requirement | Status |
|---|---|
| Code Checkout | ✅ |
| Code Stability | ✅ |
| Code Quality Analysis | ✅ |
| Code Coverage Analysis | ✅ |
| Parallel scans | ✅ |
| Option to skip scans | ✅ |
| JUnit report | ✅ |
| JaCoCo report | ✅ |
| Checkstyle report | ✅ |
| Approve / Deny publication | ✅ |
| Publish artifacts after approval | ✅ |
| Slack notification | ✅ |
| Email notification | ✅ |

---

## Conclusion

The Jenkins Declarative CI pipeline successfully implements the complete CI workflow required by the assignment.

It performs automated testing and analysis, generates reports, provides configurable scan options, requires manual approval before artifact publication, archives the generated WAR artifact, and sends notifications through both Slack and Email.