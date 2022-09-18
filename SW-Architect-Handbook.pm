# PIM-Spring Full Software Architect Reference (Microservices + Swagger + AWS)

This is a **complete architecture reference ** for the PIM-spring project, including local development, Docker, microservices architecture, Swagger/OpenAPI setup, aggregation, AWS deployment, CI/CD, and monitoring. It is designed for **long-term lookup** and full usage without additional documentation.

---

## **1. Project Overview**

**PIM-spring** is a backend system for managing complex product information across channels. It is the **single source of truth** for product data.

**Goals:**

* Centralized product data management
* Support for variants, categories, attributes, media
* Workflow-based lifecycle management
* Scalable microservices architecture
* Cloud-ready deployment with CI/CD automation

**Technology Stack:**

* **Backend:** Java 17, Spring Boot 3, Spring Data JPA (Hibernate), Maven
* **Database:** H2 (development), RDS/PostgreSQL (production)
* **Frontend Scaffold:** React, Node.js & npm
* **Containerization:** Docker
* **Orchestration:** ECS/EKS or local Kubernetes
* **CI/CD:** GitHub Actions or AWS CodePipeline/CodeBuild/CodeDeploy
* **API Documentation:** Swagger/OpenAPI per microservice and aggregated via API Gateway

---

## **2. User Stories & Epics**

Mapped to microservices:

* **Epic 1: Product Management** → Product Service
* **Epic 2: Attributes & Product Families** → Attribute & Family Services
* **Epic 3: Media Management** → Media Service
* **Epic 4: Data Quality** → Validation Service
* **Epic 5: Workflow** → Workflow Service
* **Epic 6: Channels & Export** → Channel & Export Services
* **Epic 7: Advanced Features** → Bulk & Search Services

All acceptance criteria from the original README are preserved and referenced in implementation/testing.

---

## **3. Local Development**

### Backend

```bash
cd PIM-spring/backend
mvn clean install
mvn spring-boot:run
```

* Runs on embedded **Tomcat** (port 8080)
* Swagger UI available at `http://localhost:8080/swagger-ui.html`

### Frontend

```bash
cd PIM-spring/frontend
npm install
npm start
```

* Available at `http://localhost:3000`

### Running Tests

```bash
mvn test
```

* Reports in `target/surefire-reports`

### API Example (Create Product)

```bash
curl -X POST http://localhost:8080/api/products \
-H "Content-Type: application/json" \
-d '{"sku": "LAPTOP-123", "name": "Modern Laptop"}'
```

---

## **4. Microservices Architecture**

* Each epic is implemented as an **independent microservice**.
* Each microservice exposes **its own Swagger UI and OpenAPI spec**.
* Multiple Swagger UIs per microservice is **common practice**.

### Service Mapping Example

```
Product Service          → /swagger-ui.html, /v3/api-docs
Variants Service         → /swagger-ui.html, /v3/api-docs
Attributes Service       → /swagger-ui.html, /v3/api-docs
Family Service           → /swagger-ui.html, /v3/api-docs
Media Service            → /swagger-ui.html, /v3/api-docs
Workflow Service         → /swagger-ui.html, /v3/api-docs
Channel Service          → /swagger-ui.html, /v3/api-docs
Export Service           → /swagger-ui.html, /v3/api-docs
Bulk Service             → /swagger-ui.html, /v3/api-docs
Search Service           → /swagger-ui.html, /v3/api-docs
```

### Centralized Documentation via API Gateway

1. Deploy a **Spring Cloud Gateway** or **AWS API Gateway**.
2. Configure **routes to each microservice**:

   * Example (Spring Cloud Gateway YAML snippet):

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: product-service
          uri: http://product-service:8080
          predicates:
            - Path=/product/**
        - id: media-service
          uri: http://media-service:8080
          predicates:
            - Path=/media/**
```

3. Aggregate Swagger/OpenAPI docs using `springdoc-openapi` + gateway:

```java
@Configuration
public class SwaggerAggregationConfig {

    @Bean
    public SwaggerUiConfigParameters swaggerUiConfigParameters() {
        SwaggerUiConfigParameters config = new SwaggerUiConfigParameters();
        config.addGroup("Product Service", "/product/v3/api-docs");
        config.addGroup("Media Service", "/media/v3/api-docs");
        return config;
    }
}
```

* Result: Single **aggregated Swagger UI** for all microservices.

---

## **5. Swagger/OpenAPI Setup per Microservice**

### 1. Add dependency in `pom.xml`

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

### 2. Annotate Controllers

```java
@RestController
@RequestMapping("/api/products")
@Tag(name="Product", description="Operations on products")
public class ProductController {

    @GetMapping("/{id}")
    @Operation(summary="Get product by ID")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PostMapping
    @Operation(summary="Create new product")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
    }
}
```

### 3. Access Swagger

```
http://<service-host>:<port>/swagger-ui.html
http://<service-host>:<port>/v3/api-docs
```

* Each microservice exposes **independent Swagger UI**.
* Aggregation via API Gateway creates **single portal**.

---

## **6. Dockerization**

```bash
docker build -t pim-backend ./backend
docker run -p 8080:8080 pim-backend

docker build -t pim-frontend ./frontend
docker run -p 3000:80 pim-frontend
```

* Each microservice container can be **scaled independently**.

---

## **7. AWS Deployment**

### Frontend (S3 + CloudFront)

```bash
aws s3 mb s3://pim-frontend-bucket
aws s3 sync frontend/build/ s3://pim-frontend-bucket
aws cloudfront create-distribution --origin-domain-name pim-frontend-bucket.s3.amazonaws.com
```

### Backend (ECS/EKS + ECR)

```bash
aws ecr create-repository --repository-name pim-backend
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account_id>.dkr.ecr.us-east-1.amazonaws.com

docker tag pim-backend:latest <account_id>.dkr.ecr.us-east-1.amazonaws.com/pim-backend:latest
docker push <account_id>.dkr.ecr.us-east-1.amazonaws.com/pim-backend:latest
```

* Deploy via **ECS Fargate or EKS**
* ALB/ELB handles routing; Nginx optional

---

## **8. CI/CD (GitHub Actions Example)**

```yaml
name: PIM-Spring CI/CD
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with: java-version: 17
      - run: cd backend && mvn clean package
      - run: cd frontend && npm install && npm run build
      - uses: aws-actions/configure-aws-credentials@v2
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: us-east-1
      - run: |
          $(aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account_id>.dkr.ecr.us-east-1.amazonaws.com)
          docker build -t pim-backend ./backend
          docker tag pim-backend:latest <account_id>.dkr.ecr.us-east-1.amazonaws.com/pim-backend:latest
          docker push <account_id>.dkr.ecr.us-east-1.amazonaws.com/pim-backend:latest
      - run: aws ecs update-service --cluster pim-cluster --service pim-backend-service --force-new-deployment
      - run: |
          aws s3 sync frontend/build/ s3://pim-frontend-bucket --delete
          aws cloudfront create-invalidation --distribution-id <CLOUDFRONT_ID> --paths '/*'
```

---

## **9. Monitoring & Logging**

* **CloudWatch**: ECS/EKS logs, Lambda, S3 events
* **Prometheus/Grafana**: Metrics for Kubernetes pods
* Centralized logging ensures **debugging across microservices**

---

## **10. Architecture Overview**

```
[Users]
   |
   v
[CloudFront CDN] (Frontend)
   |
   v
[Load Balancer (ALB/ELB)]
   |
   v
[ECS/EKS Cluster]
   |--- Product Service (Swagger UI + /v3/api-docs)
   |--- Variants Service (Swagger UI + /v3/api-docs)
   |--- Category Service (Swagger UI + /v3/api-docs)
   |--- Attributes Service (Swagger UI + /v3/api-docs)
   |--- Family Service (Swagger UI + /v3/api-docs)
   |--- Media Service (Swagger UI + /v3/api-docs)
   |--- Workflow Service (Swagger UI + /v3/api-docs)
   |--- Channel Service (Swagger UI + /v3/api-docs)
   |--- Export Service (Swagger UI + /v3/api-docs)
   |--- Bulk Service (Swagger UI + /v3/api-docs)
   |--- Search Service (Swagger UI + /v3/api-docs)
   |--- API Gateway aggregates Swagger into one portal
```

---

## **11. Key Points**

* Spring Boot uses embedded Tomcat; Nginx is only needed if serving frontend from a container.
* Each microservice exposes **its own Swagger UI**, with optional aggregation via API Gateway.
* AWS handles hosting, orchestration, CDN, CI/CD, and monitoring.
* Docker ensures **environment consistency**.
* Full CI/CD workflow builds, tests, and deploys backend & frontend automatically.
* Architecture supports **horizontal scaling, high availability, independent service testing, and aggregated API documentation**.


