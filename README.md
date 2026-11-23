# Price Service API

A Spring Boot service for a backend coding exercise about looking up the applicable price for a product, brand, and application date. The current phase provides the runnable project foundation and a framework-independent domain model with price selection rules. Persistence mappings, seed data, and REST endpoints will be added in later phases.

The original functional requirements are preserved in [docs/TestJava2023.txt](docs/TestJava2023.txt).

## Technology

- Java 21
- Spring Boot 3.5.16
- Maven Wrapper (Maven 3.10.1)
- Spring Web MVC, Spring Data JPA, Spring Validation
- In-memory H2 database
- SLF4J with Spring Boot's default Logback implementation
- JUnit 5 and Spring Boot Test

## Prerequisites

Install a Java 21 JDK and configure `JAVA_HOME` to point to its installation directory. Confirm the setup in a new PowerShell window:

```powershell
java -version
$env:JAVA_HOME
```

Both commands must resolve to a Java 21 JDK before running the Maven Wrapper. On Windows, Eclipse Temurin 21 can be installed with:

```powershell
winget install --id EclipseAdoptium.Temurin.21.JDK --exact
```

Close and reopen PowerShell after installation so that the updated environment variables are loaded. The included Maven Wrapper downloads Maven automatically on its first run, so a separate Maven installation is not required. The first build also downloads dependencies from Maven Central.

## Build and test on Windows

Open PowerShell in the repository root:

```powershell
.\mvnw.cmd clean test
```

To build the executable JAR:

```powershell
.\mvnw.cmd clean package
```

## Run on Windows

```powershell
.\mvnw.cmd spring-boot:run
```

Press `Ctrl+C` to stop the application. The embedded server uses port `8080` by default. There are no application endpoints in this phase.

## Architecture

The project is organized around three layers under `com.example.priceservice`:

- `domain` contains the immutable `Price` model and the `PriceSelector` domain service. It does not depend on Spring or JPA.
- `application` will contain use cases and coordination of domain operations.
- `infrastructure` will contain REST and persistence adapters, Spring and H2 configuration, and centralized exception handling.

Package placeholders preserve the intended layout in Git. They do not contain Java components yet. `docs/postman` is reserved for an importable Postman collection once the API exists.

## Domain rules

A price applies when its product and brand identifiers match and the requested date is between its start and end dates, inclusive. When multiple prices apply, the price with the highest numeric priority is selected. Selection returns an empty result when no price applies. If multiple applicable prices share the highest priority, selection reports an ambiguity because the original requirements define no tie-break rule.

Identifiers must be positive, priority and amount must not be negative, dates and monetary values are required, and the start date must not be after the end date. Monetary amounts use `BigDecimal`, dates use `LocalDateTime`, and currencies use `java.util.Currency`.

## Current scope

The application starts with an empty in-memory H2 database. JPA schema creation is ready for future entities, and SQL initialization is deferred until after JPA schema creation. Unit tests cover model invariants, inclusive date validity, product and brand matching, priority selection, empty and ambiguous results, and the five exercise scenarios using the four original records. The pricing table, persistence adapter, endpoint, error contract, endpoint integration tests, and Postman collection remain for later phases. This README will be expanded with API contracts, examples, errors, Postman usage, and integration test details when those features exist.
