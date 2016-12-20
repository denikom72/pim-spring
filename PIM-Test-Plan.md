# PIM-Spring Test Plan (Unit Test Prioritization & Test Cases & Test Pipeline )

This document defines the prioritized unit test scope for PIM-spring based on User Stories (US) and their Acceptance Criteria (AC). User Stories already contain full descriptions; therefore, this plan lists only identifiers without redundancy.

---

## 1. Test Priorities Overview

### **Priority 1 – Core Domain Logic**

```
US-101  AC1-11
US-102  AC1-8
US-103  AC1-8
US-201  AC1-8
US-202  AC1-8
US-301  AC1-9
US-401  AC1-7
US-501  AC1-8
US-601  AC1-6
US-602  AC1-8
```

### **Priority 2 – Important but Non‑Blocking**

```
US-701  AC1-3,5
US-702  AC1,4
```

### **Priority 3 – UI‑Dependent / Requires External Systems**

```
US-102  AC9
US-103  AC9
US-202  AC9
```

### **Priority 4 – Infrastructure‑Related / Complex Scenarios**

```
US-701  AC4,6,7
US-702  AC2,3,5,6
```

### **Priority 5 – Advanced / Long‑Running Operations**

```
US-701  AC8,9
US-702  AC7,8
```

---

## 2. Test Case Definitions

(Only identifiers, as detailed logic exists in User Stories)

### **Priority 1 – Core Test Cases**

```
TC-US-101-AC1
TC-US-101-AC2
TC-US-101-AC3
TC-US-101-AC4
TC-US-101-AC5
TC-US-101-AC6
TC-US-101-AC7
TC-US-101-AC8
TC-US-101-AC9
TC-US-101-AC10
TC-US-101-AC11

TC-US-102-AC1
TC-US-102-AC2
TC-US-102-AC3
TC-US-102-AC4
TC-US-102-AC5
TC-US-102-AC6
TC-US-102-AC7
TC-US-102-AC8

TC-US-103-AC1
TC-US-103-AC2
TC-US-103-AC3
TC-US-103-AC4
TC-US-103-AC5
TC-US-103-AC6
TC-US-103-AC7
TC-US-103-AC8

TC-US-201-AC1
TC-US-201-AC2
TC-US-201-AC3
TC-US-201-AC4
TC-US-201-AC5
TC-US-201-AC6
TC-US-201-AC7
TC-US-201-AC8

TC-US-202-AC1
TC-US-202-AC2
TC-US-202-AC3
TC-US-202-AC4
TC-US-202-AC5
TC-US-202-AC6
TC-US-202-AC7
TC-US-202-AC8

TC-US-301-AC1
TC-US-301-AC2
TC-US-301-AC3
TC-US-301-AC4
TC-US-301-AC5
TC-US-301-AC6
TC-US-301-AC7
TC-US-301-AC8
TC-US-301-AC9

TC-US-401-AC1
TC-US-401-AC2
TC-US-401-AC3
TC-US-401-AC4
TC-US-401-AC5
TC-US-401-AC6
TC-US-401-AC7

TC-US-501-AC1
TC-US-501-AC2
TC-US-501-AC3
TC-US-501-AC4
TC-US-501-AC5
TC-US-501-AC6
TC-US-501-AC7
TC-US-501-AC8

TC-US-601-AC1
TC-US-601-AC2
TC-US-601-AC3
TC-US-601-AC4
TC-US-601-AC5
TC-US-601-AC6

TC-US-602-AC1
TC-US-602-AC2
TC-US-602-AC3
TC-US-602-AC4
TC-US-602-AC5
TC-US-602-AC6
TC-US-602-AC7
TC-US-602-AC8
```

---

### **Priority 2 – Secondary Test Cases**

```
TC-US-701-AC1
TC-US-701-AC2
TC-US-701-AC3
TC-US-701-AC5

TC-US-702-AC1
TC-US-702-AC4
```

---

### **Priority 3 – Lower Priority Test Cases**

```
TC-US-102-AC9
TC-US-103-AC9
TC-US-202-AC9
```

---

### **Priority 4 – Extended Scenario Test Cases**

```
TC-US-701-AC4
TC-US-701-AC6
TC-US-701-AC7

TC-US-702-AC2
TC-US-702-AC3
TC-US-702-AC5
TC-US-702-AC6
```

---

### **Priority 5 – Long‑Running / Complex Test Cases**

```
TC-US-701-AC8
TC-US-701-AC9

TC-US-702-AC7
TC-US-702-AC8
```

---

**Filename suggestion:** `PIM-Spring-Test-Plan.md`

---

## 3. Test Type Recommendations (Unit, Integration, E2E, Functional)

### **Unit Tests (primary focus)**

Recommended for:

```
US-101
US-102
US-103
US-201
US-202
US-301
US-401
US-501
US-601
US-602
US-701 (AC1-5)
US-702 (AC1,4)
```

### **Integration Tests**

Required when multiple components interact (DB, services, workflow):

```
US-101 (AC10-11)
US-102 (inheritance & overrides)
US-103 (hierarchy operations)
US-201 (regex, constraints)
US-202 (family reassignment)
US-301 (thumbnail generation)
US-401 (scoring pipeline)
US-501 (workflow transitions)
US-602 (scheduled exports)
US-701 (partial failures)
US-702 (search index)
```

### **Functional Tests**

Validating complete business features end-to-end but without UI:

```
US-102 (variant rules)
US-103 (category constraints)
US-201 (attribute validation)
US-202 (required attributes)
US-301 (media rules)
US-401 (score thresholds)
US-501 (permissions)
US-601 (overrides)
US-602 (templates)
```

### **E2E / UI Tests (Cypress)**

Only for user interaction flows involving frontend:

```
US-103 AC3 (drag & drop)
US-202 AC3 (UI highlighting)
Media upload UI
Workflow approval UI
Product creation/editing forms
Search & filtering UI (US-702)
```

### **Regression Tests**

Triggered before every deployment:

```
US-101 AC1-9
US-102 AC1-8
US-201 AC1-8
US-202 AC1-8
US-301 AC1-9
US-401 AC1-7
US-501 AC1-8
US-601 AC1-6
US-602 AC1-8
```

---

## 4. Glossary of Test Types

### **Unit Test**

Tests a single class, method, or function in isolation. No database, no network.

### **Integration Test**

Tests how multiple components work together (DB + service + controller).

### **Functional Test**

Tests a business feature end-to-end *without UI*. Validates the whole workflow.

### **E2E Test (End-to-End)**

Simulates a real user using the browser (Cypress). Tests full stack.

### **Regression Test**

Set of critical tests to ensure existing functionality still works after changes.

### **Smoke Test**

Lightweight test run to verify basic application health after deployment.

### **Performance Test**

Evaluates system speed, scalability, load tolerance.

### **Security Test**

Checks authentication, authorization, injection protection, etc.

---

## 5. Test Automation Lifecycle (Tools, Stage, Automation Level)

### **Test Execution Table**

| Test Type                | Tools                                   | Trigger Stage                               | Automated?              | Notes                                            |
| ------------------------ | --------------------------------------- | ------------------------------------------- | ----------------------- | ------------------------------------------------ |
| Unit Tests               | JUnit5, Mockito                         | On every push & PR                          | **Yes**                 | Fast feedback, runs in CI pipeline.              |
| Integration Tests        | Spring Boot Test, Testcontainers        | After build, before image creation          | **Yes**                 | Requires DB/container orchestration.             |
| Functional Tests         | Postman/Newman, Karate                  | After Docker image build (pre-deployment)   | **Yes**                 | Tests API workflows without UI.                  |
| E2E Tests                | Cypress                                 | On staging deployment                       | **Yes**                 | UI tests are too slow for per‑push runs.         |
| Smoke Tests              | Bash scripts, API health checks         | Immediately after staging/production deploy | **Yes**                 | Ensures the system as a whole is alive.          |
| Regression Tests         | JUnit suites + Cypress regression suite | Nightly or before production release        | **Semi-automated**      | Can be triggered manually during major releases. |
| Performance Tests        | k6, JMeter                              | Pre-production environment                  | **Manual or Scheduled** | Often too heavy for CI.                          |
| Security Tests (Static)  | SonarQube, Snyk                         | On every push & PR                          | **Yes**                 | Code scanning for vulnerabilities.               |
| Security Tests (Dynamic) | OWASP ZAP, Burp Suite                   | On staging                                  | **Semi-automated**      | Needs review by security engineer.               |
| Penetration Tests        | Metasploit, manual tools                | Before major release                        | **Manual**              | Done by security specialists.                    |
| Load Tests               | k6, Gatling                             | Pre-production environment                  | **Manual or Scheduled** | For scaling & bottleneck detection.              |
| Chaos Tests              | Gremlin, Chaos Mesh                     | Special chaos environment                   | **Manual**              | Used only by dedicated SRE/DevOps.               |

---

## 6. CI/CD Pipeline Execution Order (Code Delivery)

### **CI – Code Integration Flow**

Triggered on: **git push / pull request**

```
1. Static Code Analysis (SonarQube, Snyk)
2. Unit Tests (JUnit5)
3. Integration Tests (Spring + Testcontainers)
4. Build Backend JAR & Frontend static bundle
5. Package Docker images
6. Functional API tests (Newman/Karate)
7. Push images to container registry
```

### **CD – Deployment Pipeline**

Triggered on: **merge to main / release tag**

```
1. Deploy to Staging (Kubernetes / ECS)
2. Run E2E Tests (Cypress)
3. Run Smoke Tests
4. Run Dynamic Security Scan (OWASP ZAP)
5. Optional: Load/Performance tests
6. Manual Approval Gate
7. Deploy to Production
8. Post-deploy Smoke Tests
```

---

## 7. CI/CD for IaC (Infrastructure as Code)

IaC pipelines run **independently** from application pipelines and validate the infrastructure itself.

### **7.1 Test Types in IaC Lifecycle**

| IaC Stage                       | Tests Performed                                                                                                        | Automated?     | Purpose                                          |
| ------------------------------- | ---------------------------------------------------------------------------------------------------------------------- | -------------- | ------------------------------------------------ |
| **Git Push / PR**               | Syntax validation, linting (terraform fmt, validate), security scan (Checkov, tfsec)                                   | Yes            | Ensures configuration is valid and secure.       |
| **Plan Stage (terraform plan)** | No functional tests; only drift detection & dependency validation                                                      | Yes            | Compares desired vs actual state.                |
| **Pre-Apply (Staging)**         | Infra smoke tests: DB reachable, VPC routes correct, IAM roles valid, S3 bucket access                                 | Yes            | Validates minimal operational readiness.         |
| **Post-Apply (Staging)**        | Infra integration tests: EC2/ECS/Kubernetes connectivity, DNS resolution, ALB/NGINX routes, Secrets Manager/KMS access | Yes            | Ensures infra components can talk to each other. |
| **Promotion to Production**     | Manual approval + optional chaos test (failover, autoscaling)                                                          | Semi-automated | Requires operator sign‑off.                      |
| **Post-Apply (Production)**     | Production smoke tests: DB connection, service discovery, load balancer health, networking                             | Yes            | Verifies production environment is stable.       |

### **7.2 CI for IaC**

Triggered on: **git push / PR to infra repo**

```
1. Lint Terraform / CloudFormation
2. Static security scan (Checkov, tfsec)
3. Provider & syntax validation
4. terraform plan (shows changes, no infra created)
5. Manual approval gate
```

### **7.3 CD for IaC**

Triggered on: **merge to main after approval**

```
1. terraform apply (Staging)
2. Staging Infra Smoke Tests:
   - Database reachable
   - VPC, subnet, routing tables valid
   - Security groups not blocking mandatory ports
   - IAM roles/policies validated
   - Secrets Manager and Parameter Store accessible
   - S3 bucket permissions correct

3. Staging Infra Integration Tests:
   - ECS/EKS services reachable internally
   - Load balancer health checks green
   - DNS records resolve correctly (Route53)
   - KMS encryption/decryption working

4. Manual promotion approval
5. terraform apply (Production)
6. Production Infra Smoke Tests
```

--- (Infrastructure as Code)
IaC pipelines run **independently** from application pipelines.

### **CI for IaC**

Triggered on: **git push / PR to infra repo**

```
1. Lint Terraform / CloudFormation
2. Static security scan (Checkov, tfsec)
3. Dependency & provider validation
4. Plan (terraform plan)
5. Manual approval gate
```

### **CD for IaC**

Triggered on: **merge to main after approval**

```
1. terraform apply (staging)
2. Integration check against staging services
3. Optional: chaos test or failover test
4. Manual promotion
5. terraform apply (production)
```

---

