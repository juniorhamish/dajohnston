# Spring Boot Guidelines

## 1. Prefer Constructor Injection over Field/Setter Injection

* Declare all the mandatory dependencies as `final` fields and inject them through the constructor.
* Spring will auto-detect if there is only one constructor, no need to add `@Autowired` on the
  constructor.
* Avoid field/setter injection in production code.

## 2. Prefer package-private over public for Spring components

* Declare Controllers, their request-handling methods, `@Configuration` classes and `@Bean` methods
  with default (package-private) visibility whenever possible. There's no obligation to make
  everything `public`.

## 3. Organize Configuration with Typed Properties

* Group application-specific configuration properties with a common prefix in
  `application.properties` or `.yml`.
* Bind them to `@ConfigurationProperties` classes with validation annotations so that the
  application will fail fast if the configuration is invalid.
* Prefer environment variables instead of profiles for passing different configuration properties
  for different environments.

## 4. Define Clear Transaction Boundaries

* Define each Service-layer method as a transactional unit.
* Annotate query-only methods with `@Transactional(readOnly = true)`.
* Annotate data-modifying methods with `@Transactional`.
* Limit the code inside each transaction to the smallest necessary scope.

## 5. Disable Open Session in View Pattern

* While using Spring Data JPA, disable the Open Session in View filter by setting
  ` spring.jpa.open-in-view=false` in `application.properties/yml.`

## 6. Separate Web Layer from Persistence Layer

* Don't expose entities directly as responses in controllers.
* Define explicit request and response record (DTO) classes instead.
* Apply Jakarta Validation annotations on your request records to enforce input rules.

## 7. Follow REST API Design Principles

* **Versioned, resource-oriented URLs:** Structure your endpoints as `/api/v{version}/resources` (
  e.g. `/api/v1/orders`).
* **Consistent patterns for collections and sub-resources:** Keep URL conventions uniform (for
  example, `/posts` for posts collection and `/posts/{slug}/comments` for comments of a specific
  post).
* **Explicit HTTP status codes via ResponseEntity:** Use `ResponseEntity<T>` to return the correct
  status (e.g. 200 OK, 201 Created, 404 Not Found) along with the response body.
* Use pagination for collection resources that may contain an unbounded number of items.
* The JSON payload must use a JSON object as a top-level data structure to allow for future
  extension.
* Use snake_case or camelCase for JSON property names consistently.

## 8. Use Command Objects for Business Operations

* Create purpose-built command records (e.g., `CreateOrderCommand`) to wrap input data.
* Accept these commands in your service methods to drive creation or update workflows.

## 9. Centralize Exception Handling

* Define a global handler class annotated with `@ControllerAdvice` (or `@RestControllerAdvice` for
  REST APIs) using `@ExceptionHandler` methods to handle specific exceptions.
* Return consistent error responses. Consider using the ProblemDetails response
  format ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457)).

## 10. Actuator

* Expose only essential actuator endpoints (such as `/health`, `/info`, `/metrics`) without
  requiring authentication. All the other actuator endpoints must be secured.

## 11. Use Testcontainers for integration tests

* Spin up real services (databases, message brokers, etc.) in your integration tests to mirror
  production environments.

## 12. Use random port for integration tests

* When writing integration tests, start the application on a random available port to avoid port
  conflicts by annotating the test class with:

    ```java
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    ```

## 13. Logging

* **Use a proper logging framework.**  
  Never use `System.out.println()` for application logging. Rely on SLF4J (or a compatible
  abstraction) and your chosen backend (Logback, Log4j2, etc.).

* **Protect sensitive data.**  
  Ensure that no credentials, personal information, or other confidential details ever appear in log
  output.

* **Guard expensive log calls.**  
  When building verbose messages at `DEBUG` or `TRACE` level, especially those involving method
  calls or complex string concatenations, wrap them in a level check or use suppliers.

## 14. Security & Identity with Auth0

* Use Auth0 for centralized identity management.
* On the backend, integrate with Spring Security OAuth2 Resource Server for JWT validation.
* On the frontend, use `@auth0/nextjs-auth0` for session management and authentication.

## 15. Infrastructure & Code Quality Tooling

* **Husky & lint-staged:** Automatically runs code quality checks (like Biome or Spotless) on
  changed files before every `git commit`.
* **Spotless:** Enforces Java formatting and import organization using `google-java-format`.

## 16. Baseline Versions

* **Java:** 26
* **Spring Boot:** 4.0.5
* **Next.js:** 16

Always use the latest versions of any new dependencies. Double check in maven central or npm
registry for the latest versions.

## 17. Unit testing

* Code should be tested in the defined layers in isolation (controller, service, repository)
* Unit tests should be written in JUnit 5 and Mockito.
* Integration tests should be written in JUnit 5 and Spring Boot Test.
* Test coverage should be 100%.
* Creating UUIDs for testing should be done using `UUID.fromString()` with a random UUID string.

## Next.js 16

- Use `proxy.ts` instead of `middleware.ts` for request interception
- Turbopack is the default bundler
- `params`, `searchParams`, `cookies()`, `headers()`, `draftMode()` are async — always `await` them
- Use `"use cache"` directive for server-side caching
- TypeScript: write erasableSyntaxOnly compliant code (no enums, namespaces, class parameter
  properties)
- Use vitest + react testing library for tests

# API Guidelines

- Define APIs using OpenAPI 3.1.1 [specification](https://spec.openapis.org/oas/v3.1.1).
- Share the same spec file between frontend and backend.
- Autogenerate code from the spec file.
    - Using [OpenAPI Processor](openapiprocessor.io) for Java and gradle,
    - Using [Hey API](https://heyapi.dev/openapi-ts/clients/next-js) for Next.js.
- Maintain separate files for each distinct API.
- Include auth details in the spec file.

Generated client code is in frontend/src/generated/ — do not read or modify; use api/v1/portal.yaml
as the API reference.

## Reference Docs (read only when relevant to the task)

- Architecture: docs/architecture.md
- Auth config: docs/authentication_configuration.md
- Testing strategy: docs/testing_strategy.md
- Deployment: docs/deployment.md
- Local development: docs/local_development.md
- Implementation plan: docs/implementation_plan.md
- Decision log: docs/decision_log.md
- Code quality: docs/code_quality.md
- Monitoring & logging: docs/monitoring_logging.md
- Cloud integration tests: docs/cloud_integration_tests.md
- Exposing services: docs/exposing_services.md
- Technology choices: docs/technology_choices.md

## Project Structure Map

- backend/portal/ — Spring Boot app (Java 26)
    - app/ — App domain (entity, service, controller, mapper)
    - user/ — User domain (entity, service, controller, Auth0 integration)
    - household/ — Household domain (entities, service, controller, invitations)
    - config/ — Tenant interceptor, RLS, web config, logging
- backend/shared-security/ — Shared OAuth2/JWT security config
- frontend/src/ — Next.js 16 app
    - generated/ — DO NOT READ (auto-generated from OpenAPI)
    - lib/ — Auth0, test utils
    - api/ — Hey API config
- api/v1/portal.yaml — OpenAPI spec (source of truth for API contracts)
- infra/ — Terraform (Vercel, Neon, Auth0, SonarCloud)
- docs/ — Project documentation
    - architecture.md — High-level architecture overview
    - authentication_configuration.md — Auth0 configuration details
    - testing_strategy.md — Testing approach and guidelines
    - deployment.md — Deployment process and considerations

## Junie Instructions

- never assume, always ask for clarification if there is ambiguity or multiple options
- don't repeatedly attempt to run the same command if you are having issues, stop and ask for help
