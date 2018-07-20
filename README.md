# PIM-spring: Product Information Management System

PIM-spring is a robust backend application for a Product Information Management (PIM) system, built with Java and the Spring Boot framework. It provides a centralized platform to manage, enrich, and distribute complex product information across various channels.

This system is designed to be the single source of truth for all product data, ensuring consistency and accuracy. It supports complex product structures, including variants, categories, and custom attributes, and provides workflows for managing the product lifecycle.

## What is a PIM?

A Product Information Management (PIM) system helps businesses manage all the data, content, and other materials that are needed to market and sell products. A PIM provides a central hub to create a product catalog and is a tool for collaboratively managing all your product information. It simplifies the process of creating, maintaining, and distributing compelling product experiences.

## Features

This PIM backend provides a wide range of features, organized by epics:

*   **Product Management:** Create, update, and manage products with a rich data model.
*   **Product Variants:** Define products with multiple variations (e.g., size, color).
*   **Categorization:** Organize products into hierarchical categories.
*   **Attributes & Families:** Create custom attributes and group them into product families to enforce data consistency.
*   **Media Management:** Upload and associate images with products.
*   **Data Quality:** Automatically calculate a completeness score for products to ensure data richness.
*   **Workflow:** Manage the product lifecycle with a simple review and approval workflow.
*   **Channels:** Define different sales channels and manage channel-specific product data overrides.
*   **Exporting:** Create templates for exporting product data to different formats.
*   **Bulk Operations:** Perform asynchronous bulk creation and updates on products.
*   **Search:** A simple search functionality to find products by name or SKU.

## Technology Stack

*   **Backend:**
    *   Java 17
    *   Spring Boot 3
    *   Spring Data JPA (Hibernate)
    *   Maven (Dependency Management)
*   **Database:**
    *   H2 (In-memory database for development)
*   **Frontend (Scaffolded):**
    *   React
    *   Node.js & npm

## Prerequisites

Before you begin, ensure you have the following installed:
*   JDK 17 or later
*   Apache Maven 3.6 or later
*   Node.js and npm (for the frontend)

## Installation & Setup

### Backend

1.  **Navigate to the backend directory:**
    ```bash
    cd PIM-spring/backend
    ```

2.  **Build the project using Maven:**
    This command will compile the source code, run tests, and package the application into a JAR file.
    ```bash
    mvn clean install
    ```

3.  **Run the Spring Boot application:**
    ```bash
    mvn spring-boot:run
    ```
    The backend server will start on `http://localhost:8080`.

### Frontend

1.  **Navigate to the frontend directory:**
    ```bash
    cd PIM-spring/frontend
    ```

2.  **Install the dependencies:**
    ```bash
    npm install
    ```

3.  **Start the React development server:**
    ```bash
    npm start
    ```
    The frontend application will be available at `http://localhost:3000`.

## API Usage

The API is documented via the endpoints themselves. You can use tools like `curl` or Postman to interact with the API.

**Example: Create a Product**

Here is an example of how to create a new product using `curl`:

```bash
curl -X POST http://localhost:8080/api/products \
-H "Content-Type: application/json" \
-d 
'{ 
    "sku": "LAPTOP-123",
    "name": "Modern Laptop",
    "description": "A powerful and modern laptop for all your needs."
}'
```

## Running Tests

To run the backend unit and integration tests, navigate to the `PIM-spring/backend` directory and run the following Maven command:

```bash
mvn test
```

This will execute all tests in the `src/test/java` directory and generate a report in the `target/surefire-reports` directory.

## Project Roadmap & User Stories

This section outlines the implemented and planned features for the PIM system, based on the provided user stories.

*   `- [x]` Completed
*   `- [ ]` Not Yet Implemented

---

### **Epic 1: Product Management**

#### **US-101: Create Product**
**Acceptance Criteria:**
- [x] User can create product with mandatory fields
- [x] System assigns auto-increment `product_id`
- [x] Default status: `draft`
- [x] Audit logs capture actions
- [x] Completeness score calculated automatically
- [x] System rejects duplicate SKU with clear error
- [x] Validation fails for invalid SKU format
- [x] System prevents saving when required fields are empty
- [x] Database connection failure triggers graceful error
- [x] Concurrent edit detection
- [x] Bulk import fails partially with detailed error report

---

#### **US-102: Product Variants**
**Acceptance Criteria:**
- [x] Variants inherit parent attributes
- [x] Variant-specific overrides work correctly
- [x] Auto-SKU generation based on pattern
- [x] Prevents variant creation without differentiating attributes
- [x] Blocks parent publication if variants incomplete
- [x] Handles invalid pattern in auto-SKU generation
- [x] Prevents circular variant relationships
- [x] Validates variant combination uniqueness
- [ ] Manages inventory conflicts during variant deletion (Requires inventory system)

---

#### **US-103: Product Categories**
**Acceptance Criteria:**
- [x] Creates hierarchical categories
- [x] Multiple category assignments work
- [ ] Drag-and-drop reordering functions (Frontend task)
- [x] Prevents category deletion with products
- [x] Validates maximum category depth (5 levels)
- [x] Handles invalid category slugs
- [x] Prevents duplicate category names per level
- [x] Manages orphaned products during category operations

---

### **Epic 2: Attributes & Product Families**

#### **US-201: Define Attributes**
**Acceptance Criteria:**
- [x] Creates all attribute types correctly
- [x] Validation rules enforce data integrity
- [x] Attribute grouping works as expected (via Product Families)
- [x] Rejects duplicate attribute codes immediately
- [x] Validates regex patterns
- [x] Prevents attribute deletion when used in products
- [ ] Handles invalid default values for attribute type
- [ ] Manages constraint violations during attribute updates

---

#### **US-202: Product Families**
**Acceptance Criteria:**
- [x] Product families enforce attribute sets
- [x] Required attribute validation works
- [ ] UI correctly highlights missing fields (Frontend task)
- [x] Prevents family deletion when assigned to products
- [ ] Validates attribute compatibility during family changes
- [ ] Handles data migration failures during family reassignment
- [ ] Manages orphaned attributes after family modifications

---

### **Epic 3: Media Management**

#### **US-301: Upload Images**
**Acceptance Criteria:**
- [x] Successful upload of supported formats
- [ ] Thumbnail generation works automatically
- [ ] Image sorting and primary selection functional
- [x] Rejects unsupported formats
- [x] Blocks oversized files
- [ ] Prevents exceeding image limit
- [ ] Handles corrupt image files gracefully
- [ ] Manages storage quota exceeded scenarios
- [ ] Recovers from failed thumbnail generation

---

### **Epic 4: Data Quality**

#### **US-401: Completeness Score**
**Acceptance Criteria:**
- [x] Score calculates correctly 0-100
- [x] Real-time updates work smoothly
- [x] Publishing blocks when below threshold
- [x] Handles division by zero in score calculation
- [x] Manages missing attribute definitions gracefully
- [x] Validates threshold configuration (0-100)
- [ ] Recovers from calculation failures

---

### **Epic 5: Workflow**

#### **US-501: Product Review Workflow**
**Acceptance Criteria:**
- [x] State transitions work correctly
- [ ] Role-based permissions enforce workflow
- [ ] Notifications sent appropriately
- [ ] Prevents unauthorized state transitions
- [ ] Validates reviewer assignments exist
- [ ] Handles missing required approvals
- [ ] Manages workflow deadlocks
- [ ] Recovers from notification service failures

---

### **Epic 6: Channels & Export**

#### **US-601: Channels**
**Acceptance Criteria:**
- [x] Channel-specific overrides work correctly
- [ ] Export filters published/complete products only
- [x] Validates channel configuration before activation
- [ ] Handles external system connectivity issues
- [ ] Manages data mapping failures during export
- [ ] Prevents channel deletion with active exports

---

#### **US-602: Export Templates**
**Acceptance Criteria:**
- [x] Template creation and mapping work
- [ ] Scheduled exports execute reliably
- [x] Logs capture export activities
- [ ] Validates template syntax before saving
- [ ] Handles malformed data during export
- [ ] Manages large export memory issues
- [ ] Recovers from interrupted export jobs
- [ ] Notifies on export failures with specific errors

---

### **Epic 7: Advanced Features**

#### **US-701: Bulk Operations**
**Acceptance Criteria:**
- [x] Bulk updates process efficiently
- [x] Progress tracking shows accurate ETA
- [ ] Rollback functions correctly
- [ ] Validates bulk operation size limits
- [x] Handles partial failures in bulk operations
- [ ] Manages transaction timeouts
- [ ] Prevents data corruption during rollback
- [ ] Validates user permissions for bulk actions

---

#### **US-702: Search & Filtering**
**Acceptance Criteria:**
- [x] Search returns relevant results quickly
- [ ] Filters apply correctly
- [ ] Exports contain accurate data
- [x] Handles special characters in search queries
- [ ] Manages search index corruption
- [ ] Validates filter combinations
- [ ] Prevents timeout on large result sets
- [ ] Handles export file generation failures
