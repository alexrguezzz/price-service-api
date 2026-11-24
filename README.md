# Price Service API

A runnable Spring Boot REST service that returns the applicable final price for a product, brand, and local application date. The original exercise is preserved in [docs/TestJava2023.txt](docs/TestJava2023.txt).

## Requirements and commands

Install a Java 21 JDK and set `JAVA_HOME` to that JDK. The included Maven Wrapper downloads Maven and project dependencies on its first run; no separate Maven installation is required.

### Windows (PowerShell)

From the repository root:

```powershell
java -version
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

### Linux / macOS

From the repository root:

```bash
java -version
./mvnw clean verify
./mvnw spring-boot:run
```

If the Maven Wrapper does not have execution permissions, run `chmod +x mvnw` before executing the commands.

The build creates an executable JAR at `target/price-service-api-0.0.1-SNAPSHOT.jar`. You can also run it with `java -jar target/price-service-api-0.0.1-SNAPSHOT.jar`. Stop the running service with `Ctrl+C`. It listens on port 8080 by default.

## REST API

`GET /api/v1/prices` accepts three required query parameters:

| Parameter | Meaning | Format |
| --- | --- | --- |
| `applicationDate` | Local application date and time | ISO 8601 local date-time, e.g. `2020-06-14T16:00:00` |
| `productId` | Product identifier | Positive integer |
| `brandId` | Brand identifier | Positive integer |

Example request in PowerShell:

```powershell
Invoke-RestMethod 'http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1'
```

Successful response (`200 OK`):

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceListId": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "currency": "EUR"
}
```

Dates have no time-zone offset because the exercise supplies local timestamps. The amount uses `BigDecimal` internally and the response includes its ISO currency code. The storage-only JPA `ID` is never exposed.

| Status | Meaning |
| --- | --- |
| `200 OK` | One applicable price was selected. |
| `400 Bad Request` | A parameter is missing, malformed, or a product or brand ID is not positive. |
| `404 Not Found` | No applicable price exists for the requested date, product, and brand. |
| `405 Method Not Allowed` | The endpoint does not support the requested HTTP method. The `Allow` header lists supported methods. |
| `406 Not Acceptable` | The requested `Accept` media type cannot be returned. |
| `409 Conflict` | Multiple applicable prices share the highest priority. |
| `500 Internal Server Error` | An unexpected failure occurred. The response does not expose internal details. |

Errors use Spring `ProblemDetail` (RFC 9457) with `type`, `title`, `status`, `detail`, and request `instance` fields. For example, an unknown product returns:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "No applicable price was found for the requested product, brand, and date",
  "instance": "/api/v1/prices"
}
```

## Selection and architecture

The service uses a simple hexagonal structure under `com.example.priceservice`:

- `domain` contains the immutable `Price` model, the `PriceQueryPort` output port, and `PriceSelector`. It has no Spring or JPA dependency.
- `application` contains `GetApplicablePriceService`, which obtains candidates through the port and delegates selection to the domain service. It reports a missing price without depending on HTTP.
- `infrastructure.persistence` implements the port with Spring Data JPA and maps JPA rows into domain prices.
- `infrastructure.rest` validates the HTTP query, maps the selected price into a response DTO, and handles exceptions centrally.
- `infrastructure.config` wires the domain selector and application service as Spring beans.

A price is applicable when product and brand match and the application date falls within its start and end dates, inclusive. The highest numeric priority wins. A tie at the highest priority produces `409 Conflict`; the exercise does not specify a tie-break rule.

## Data and tests

H2 runs in memory. Hibernate creates the `PRICES` table on startup, then Spring Boot loads the four original prices from `src/main/resources/data.sql`. No external database or manual initialization is needed. Data is lost when the process stops. The table includes a generated technical `ID` for JPA; it is absent from the domain model and API.


Run all unit and integration tests using the Maven Wrapper command for your operating system, as shown in the [Requirements and commands](#requirements-and-commands) section.
 Domain tests cover invariants and selection rules. Application tests verify port use, selector delegation, priority selection, missing prices, and ambiguity without starting Spring. Persistence tests verify H2 seed data and query mapping. MockMvc integration tests use the full Spring context and seeded H2 data to assert all response fields for the five exercise scenarios, validation errors, missing prices, unsupported methods and response media types, and an isolated ambiguity case. The ambiguity row is rolled back after its test.

An importable collection with the five scenarios, one 404 request, and one 400 request is in [docs/postman/Price Service API.postman_collection.json](docs/postman/Price%20Service%20API.postman_collection.json). Its `baseUrl` variable defaults to `http://localhost:8080`.

## Technical decisions

- `LocalDateTime` represents the source timestamps without inventing a time zone; JSON uses ISO 8601.
- `BigDecimal` and a `DECIMAL(19,2)` database column preserve cent precision.
- The repository filters by product, brand, and inclusive date bounds; the domain selector alone applies priority.
- `ProblemDetail` gives every handled error the same standard contract. Expected misses and ambiguous selections are logged without routine stack traces; unexpected failures are logged server-side and return a safe message.
