# Price Service API

A Spring Boot service for a backend coding exercise about looking up the applicable price for a product, brand, and application date. This first phase provides only the runnable project foundation. Price selection, persistence mappings, seed data, and REST endpoints will be added in later phases.

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

- `domain` will contain plain Java domain models, business rules, and output ports. It must not depend on Spring or JPA.
- `application` will contain use cases and coordination of domain operations.
- `infrastructure` will contain REST and persistence adapters, Spring and H2 configuration, and centralized exception handling.

Package placeholders preserve the intended layout in Git. They do not contain Java components yet. `docs/postman` is reserved for an importable Postman collection once the API exists.

## Current scope

The application starts with an empty in-memory H2 database. JPA schema creation is ready for future entities, and SQL initialization is deferred until after JPA schema creation. A single context-loading test checks the initial Spring configuration. The pricing table, sample records, price selection rules, endpoint, error contract, unit tests, five required endpoint integration scenarios, and Postman collection remain for later phases. This README will be expanded with API contracts, examples, priority behavior, errors, Postman usage, and test details when those features exist.
