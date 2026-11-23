# Repository instructions

## Requirements

- Use `docs/TestJava2023.txt` as the primary source of functional requirements.
- Follow the additional mandatory requirements and implementation recommendations established for this project: a fully runnable REST microservice, sound REST design, unit and integration tests including all five specified scenarios, English documentation, an importable Postman collection, hexagonal architecture with DDD principles, logging, and centralized exception handling.
- Ensure that a reviewer can build, test, and run the microservice without manual project adjustments.
- Implement the requested behavior exactly and avoid unrelated features.
- Limit changes to the phase requested by the user. Do not implement future phases early.

## Language and conventions

- Write code, identifiers, comments, documentation, and log messages in English.
- Follow Java and Spring Boot conventions; use descriptive names and readable code.
- Apply SOLID principles where they add value. Avoid overengineering, unused abstractions, and empty Java components.
- Keep this project as a single Maven module. Do not use Lombok, Docker, authentication, authorization, external runtime services, or persistent databases.

## Architecture

- Maintain a simple hexagonal structure with DDD principles and separate domain, application, and infrastructure responsibilities.
- Keep domain code independent of Spring, JPA, and other infrastructure frameworks.
- Place domain models, rules, and output ports in `domain`; use cases and coordination in `application`; REST adapters, persistence adapters, configuration, and exception handling in `infrastructure`.
- Introduce components only when a requested feature needs them.

## REST API

- Design endpoints with REST conventions, appropriate HTTP methods and status codes, and clear, consistent request and response contracts.
- Validate input parameters and return consistent error responses.

## Persistence

- Use an in-memory H2 database.
- When persistence is implemented, initialize it with the four price records in `docs/TestJava2023.txt`.
- Select prices using the application date, product, brand, and highest matching priority specified by the exercise.

## Testing

- Add unit tests for business rules and integration tests for the REST endpoint when those features are implemented.
- Cover all five scenarios from the exercise with endpoint integration tests.
- Run relevant tests after meaningful changes. Never claim a feature works without verifying it.

## Logging and exceptions

- Use SLF4J for relevant service operations without logging unnecessary or sensitive information.
- Implement centralized exception handling and consistent error responses when the API is added.

## Documentation

- Keep `README.md` aligned with implemented behavior. Add endpoint contracts, request and response examples, priority rules, error behavior, and test details as they become available.
- Add an importable Postman collection when endpoints are implemented.
- Record relevant technical decisions and keep the original exercise text unchanged.

## Git workflow

- Do not create or delete branches, switch branches, commit, push, merge, or alter Git history.
- Work only on the current branch and within the requested phase.
- Inspect existing files before overwriting them.
- Report changed files and propose a Conventional Commits message at the end of each phase.
