# Price Service API

A Spring Boot service for a backend coding exercise about looking up the applicable price for a product, brand, and application date. The current phase provides the framework-independent domain rules and an H2 persistence adapter seeded with the exercise prices. The REST API is planned for a later phase.

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

- `domain` contains the immutable `Price` model, the `PriceSelector` domain service, and the `PriceQueryPort` output port. It does not depend on Spring or JPA.
- `application` will contain use cases and coordination of domain operations.
- `infrastructure.persistence` contains the JPA entity, Spring Data repository, and adapter that implements the output port. Future REST adapters and HTTP exception handling will also belong in infrastructure.

Empty package placeholders reserve space for later application and API components. `docs/postman` is reserved for an importable Postman collection once the API exists.

## Domain rules

A price applies when its product and brand identifiers match and the requested date is between its start and end dates, inclusive. When multiple prices apply, the price with the highest numeric priority is selected. Selection returns an empty result when no price applies. If multiple applicable prices share the highest priority, selection reports an ambiguity because the original requirements define no tie-break rule.

Identifiers must be positive, priority and amount must not be negative, dates and monetary values are required, and the start date must not be after the end date. Monetary amounts use `BigDecimal`, dates use `LocalDateTime`, and currencies use `java.util.Currency`.

## Persistence and initialization

Hibernate creates the `PRICES` table in the in-memory H2 database at startup (`ddl-auto: create-drop`). Spring Boot then runs `src/main/resources/data.sql` because `spring.jpa.defer-datasource-initialization` is enabled. The script inserts the four original records from the exercise without any manual setup. This also applies to tests. The database exists only while the application is running.

The table maps `BRAND_ID`, `START_DATE`, `END_DATE`, `PRICE_LIST`, `PRODUCT_ID`, `PRIORITY`, `PRICE`, and `CURR` explicitly. Dates use `LocalDateTime`, which represents the exercise's local timestamps without a time zone. Prices use `BigDecimal` and a `DECIMAL(19,2)` column to retain cent precision. Currency codes are stored as three-character strings and converted to `Currency` in the adapter.

JPA requires an entity identity, so `PRICES` has an auto-generated `ID` column in addition to the exercise columns. This is a storage-only identifier; it is not added to the domain `Price` record. `PRICE_LIST` has no uniqueness constraint because the exercise does not guarantee global uniqueness. The `PriceQueryPort` is in `domain.port`, following the repository's dependency rule: application code can depend on it without importing Spring or persistence classes. The JPA repository filters by product, brand, and inclusive start and end dates. It returns every matching row, including overlapping tariffs. The adapter maps those rows to domain `Price` values; `PriceSelector` alone applies the highest-priority rule and reports an ambiguity for equal highest priorities.

## Tests and current scope

The existing domain unit tests cover model invariants, inclusive dates, matching, priority, empty and ambiguous results, and the five exercise scenarios. H2 persistence integration tests verify seed loading, product and brand filters, exact date boundaries, out-of-range exclusion, field and amount mapping, overlapping candidates, and selection of the expected price for all five scenarios from stored data. Additional test rows are rolled back after each test and are never part of normal application startup.

The application use case, REST endpoint, HTTP error contract, endpoint integration tests, and Postman collection belong to later phases. The application starts and initializes H2, but it does not yet expose a price lookup endpoint.
