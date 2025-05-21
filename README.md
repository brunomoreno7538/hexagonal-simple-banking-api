# Core Banking API

## Overview

This is a RESTful API for a simplified Core Banking system. It manages users (admins, merchants, merchant users), accounts, and transactions. The project is built with Java 17, Spring Boot, and follows Hexagonal Architecture principles.

## Architecture: Hexagonal (Ports & Adapters)

The project uses Hexagonal Architecture to separate core business logic from infrastructure concerns.

* **Domain Layer**: Contains the core business logic, entities (`CoreUser`, `Merchant`, `Account`, `Transaction`), and domain services. It has no dependencies on other layers.
* **Application Layer**: Orchestrates the application's use cases (e.g., creating a user, processing a transaction). It defines input ports (use case interfaces) and output ports (repository interfaces). It depends only on the Domain layer.
* **Infrastructure Layer**: Provides concrete implementations for the ports defined by the Application layer. This includes:
    * **Inbound Adapters**: Handle requests from the outside world (e.g., REST Controllers for the HTTP API).
    * **Outbound Adapters**: Interact with external systems (e.g., JPA repositories for database interaction, JWT utilities for security).
    * Also contains configurations (Spring beans, security, OpenAPI).

## Technologies Used

* Java 17
* Spring Boot (Ensure you are using a stable version, e.g., 3.2.x or 3.3.x)
* Spring Security (with JWT for authentication)
* Spring Data JPA
* H2 Database (in-memory)
* Springdoc OpenAPI (for Swagger documentation)
* Maven

## Prerequisites

* JDK 17 or later
* Apache Maven 3.6+

## Running the Application

1.  **Clone the repository** (if applicable):
    ```bash
    git clone https://github.com/brunomoreno7538/hexagonal-simple-banking-api
    cd hexagonal-simple-banking-api
    ```

2.  **Build the project using Maven:**
    ```bash
    mvn clean install
    ```

3.  **Run the application:**
    * Using the JAR file:
        ```bash
        java -jar target/corebanking_natixis-0.0.1-SNAPSHOT.jar
        ```
    * Or, you can run the main application class (`CorebankingNatixisApplication.java`) directly from your IDE.

The application will start by default on `http://localhost:8080`.

### Running with Docker

```bash
# Build the image (multi-stage)
docker build -t corebanking_natixis .

# Run the container, mapping port 8080
docker run --rm -p 8080:8080 corebanking_natixis
```

### Initial Admin User
An initial admin user is created on startup by the `DataInitializer` class:
* **Username:** `admin`
* **Password:** `123456`

### Initial Merchant User
An initial merchant user is created on startup by the `DataInitializer` class:
* **Username:** `merchant`
* **Password:** `123456`

Use these credentials to log in via the `/api/v1/auth/login` endpoint to get a JWT token for accessing protected admin endpoints.

## API Documentation (Swagger UI)

API documentation is generated using Springdoc OpenAPI and is available via Swagger UI.

1.  **Access Swagger UI:**
    Open your browser and navigate to:
    `http://localhost:8080/swagger-ui.html`

2.  **Access OpenAPI Specification (JSON):**
    `http://localhost:8080/v3/api-docs`

3.  **Authorizing in Swagger UI:**
    * First, obtain a JWT token by making a `POST` request to `/api/v1/auth/login` with valid user credentials (e.g., the `initialadmin`).
    * In the Swagger UI, click the "Authorize" button (usually top right).
    * In the "bearerAuth" dialog, paste your JWT token in the format: `Bearer <your_jwt_token>`.
    * Click "Authorize" and then "Close". You can now test protected endpoints.

## H2 Database Console

You can access the H2 in-memory database console to view tables and run queries.

1.  **Access H2 Console:**
    Open your browser and navigate to:
    `http://localhost:8080/h2-console`

2.  **Connection Details:**
    * **JDBC URL:** `jdbc:h2:mem:corebankingdb` (or as defined in your `application.properties`)
    * **User Name:** `sa`
    * **Password:** `password`

Click "Connect" to access the database.
