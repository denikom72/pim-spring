# **PIM – IaC Test Plan (Terraform + Optional Ansible)**

This document defines testing strategy, testing stages, tools, and CI/CD flow for Infrastructure-as-Code (IaC) within the PIM Platform. It covers Terraform-based provisioning ( NEW: Testphase to avoid configure based provisioning via puppet, chef, ... ) on AWS and optional Ansible server configuration.

---

# **1. IaC CI/CD Overview**

Infrastructure is delivered through two independent pipelines:

### **1. Terraform Pipeline (IaC)**

Used to provision AWS infrastructure:

* VPC
* Subnets / Routing
* Security Groups
* RDS
* EKS / ECS / EC2
* S3 / CloudFront / API Gateway
* IAM Roles & Policies

### **2. Ansible Pipeline (Optional)**

Used *only* when EC2-based services require OS-level configuration:

* package installation
* app service configuration
* user management
* file templating
* Docker engine installation

---

# **2. IaC Test Stages & Automation Levels**

This table defines **what is tested, when, with which tools, and whether it is automated.**

## **2.1 Terraform Pipeline Testing Matrix**

| Stage                    | Tests                   | Tools                         | Automated | Notes                    |
| ------------------------ | ----------------------- | ----------------------------- | --------- | ------------------------ |
| **CI – on Git Push**     | Formatting checks       | terraform fmt                 | ✓         | No infra created         |
|                          | Static analysis         | tflint                        | ✓         | Rules for AWS resources  |
|                          | Security scanning       | tfsec / checkov               | ✓         | Detect bad IAM, open SGs |
|                          | Dependency validation   | terraform init -backend=false | ✓         | Ensures modules load     |
|                          | Dry-run plan            | terraform plan                | ✓         | No apply yet             |
| **CD – Stage Apply**     | Resource creation       | terraform apply               | ✓         | Applies to staging       |
|                          | Infra smoke tests       | AWS CLI, Terratest            | ✓         | VPC, subnets, SGs        |
|                          | DNS resolution          | dig / AWS CLI                 | ✓         | Route53 A/AAAA/CNAME     |
|                          | LB health               | AWS CLI ELBv2                 | ✓         | Target health            |
|                          | DB connection           | psql/mysql-client             | ✓         | Connectivity only        |
|                          | IAM access simulation   | AWS IAM Simulator             | ✓         | Ensures least privilege  |
|                          | S3 availability         | aws s3 ls                     | ✓         | Bucket exists            |
|                          | EKS cluster health      | aws eks describe-cluster      | ✓         | Checks active state      |
| **CD – Manual Approval** | Change review           | Manual                        | ✗         | Required for prod        |
|                          | Security approval       | Manual                        | ✗         | IAM & networking         |
| **CD – Prod Apply**      | Production apply        | terraform apply               | ✓         | Executed by pipeline     |
|                          | Post-deploy smoke tests | Terratest/AWS CLI             | ✓         | Same tests as staging    |
|                          | Rollback validation     | Manual                        | ✗         | If failure detected      |

---

## **2.2 Optional Ansible Pipeline Testing Matrix**

Executed **only if EC2 servers are used**.

| Stage                     | Tests                | Tools              | Automated | Notes                   |
| ------------------------- | -------------------- | ------------------ | --------- | ----------------------- |
| **CI – Git Push**         | Syntax check         | ansible-lint       | ✓         | No hosts touched        |
|                           | Security check       | ansible-lint rules | ✓         | Detect unsafe practices |
| **CD – Staging Apply**    | Playbook execution   | ansible-playbook   | ✓         | Config deployed         |
|                           | Service health       | curl, systemctl    | ✓         | API reachable           |
|                           | Package verification | ansible facts      | ✓         | Docker, Java, etc.      |
|                           | File integrity       | checksum/sha       | ✓         | Templates deployed      |
| **CD – Manual Approval**  | Review               | Manual             | ✗         | Required before prod    |
| **CD – Production Apply** | Apply                | ansible-playbook   | ✓         | Final rollout           |
|                           | Post-deploy checks   | curl/systemctl     | ✓         | Verify service          |

---

# **3. Deployment Flow (Terraform IaC)**

Step-by-step process for infra delivery.

## **3.1 CI Pipeline (Triggered on PR or Push)**

1. terraform fmt
2. terraform validate
3. tflint
4. tfsec / checkov
5. terraform plan
6. Store plan artifact

➡ **No infrastructure is created at this stage.**

---

## **3.2 CD Pipeline – Apply to Staging**

1. terraform init (backend: S3 + DynamoDB lock)
2. terraform apply -auto-approve (staging workspace)
3. Run infra smoke tests

   * VPC routing
   * Security Group reachability
   * EKS health
   * RDS connection
   * S3 bucket existence
4. Conditional approval step

➡ Staging becomes the **validation environment**.

---

## **3.3 Manual Approval for Production**

Security/governance approves the change.

* Review plan differences
* Review IAM changes
* Check potential downtime
* Validate risk score

➡ After approval → pipeline continues.

---

## **3.4 CD Pipeline – Apply to Production**

1. terraform init
2. terraform apply -auto-approve (production workspace)
3. Run production smoke tests
4. If failure → manual rollback using `terraform state` or previous plan

---

# **4. IaC Test Types**

Short definitions for quick reference.

### **4.1 Static Tests (CI)**

Run before any infra is created.

* terraform fmt
* terraform validate
* tflint
* tfsec
* checkov

### **4.2 Smoke Tests (post-apply)**

Basic availability checks:

* DNS resolution
* ALB health targets
* S3 bucket accessible
* EKS cluster ACTIVE
* RDS accessible

### **4.3 Infrastructure Integration Tests**

Validates resource interaction:

* EC2 ↔ RDS connectivity
* Pod ↔ RDS connectivity
* ALB ↔ Service mesh routing
* IAM assume-role tests

### **4.4 Security Tests**

* IAM policy simulation
* KMS encryption validation
* Security Group reachability
* Public S3 detection

### **4.5 Disaster Recovery Tests (manual or scheduled)**

* RDS snapshot restore
* S3 versioning restore
* EKS node replacement

---

# **5. When Each Test Type Runs**

| Stage             | Static Tests | Infra Tests | Security Tests | Integration Tests | Manual Tests |
| ----------------- | ------------ | ----------- | -------------- | ----------------- | ------------ |
| **Push/PR (CI)**  | ✓            | ✗           | ✓              | ✗                 | ✗            |
| **Staging Apply** | ✓            | ✓           | ✓              | ✓                 | ✗            |
| **Prod Approval** | ✓            | ✗           | ✓              | ✗                 | ✓            |
| **Prod Apply**    | ✓            | ✓           | ✓              | ✓                 | Limited      |

---

# **6. CI/CD Pipelines Summary**

## **6.1 Code CI/CD (Apps, Services)**

* CI: Build → Test → Scan → Package Docker
* CD: Deploy to Staging → E2E tests → Deploy to Prod

## **6.2 IaC CI/CD (Terraform)**

* CI: Lint → Validate → Security Scan → Plan
* CD: Apply Staging → Infra Tests → Manual Approval → Apply Prod

## **6.3 IaC CD (Ansible)** *(optional)*

* CI: Syntax + lint only
* CD: Apply Staging → Service Tests → Approval → Apply Prod

---

# **7. Glossary**

**Static tests** – Validate code correctness without creating infra.

**Smoke tests** – Quick checks verifying core components are alive.

**Integration tests** – Validate network and service dependencies between cloud resources.

**Security tests** – IAM, encryption, SG, and policy validations.

**Post-deploy tests** – Run after staging/prod apply to confirm correct provisioning.

**Manual approval** – Required step before production for compliance.

**Terratest** – Go-based infra testing framework used by DevOps teams.

**Ansible** – Configuration tool for servers (optional in containerized setups).

**Terraform** – Industry standard IaC tool for provisioning AWS.

---

