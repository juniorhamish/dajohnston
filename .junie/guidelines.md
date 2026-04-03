# Java Guidelines

These are the general guidelines for writing Java code.

## Table of Contents

- [1. Naming Conventions](#1-naming-conventions)
- [2. Code Layout](#2-code-layout)
- [3. Best Practices for Classes, Interfaces, and Enums](#3-best-practices-for-classes-interfaces-and-enums)
- [4. Exception Handling](#4-exception-handling)
- [5. Concurrency](#5-concurrency)
- [6. Use of `Optional`](#6-use-of-optional)
- [7. Stream API Best Practices](#7-stream-api-best-practices)
- [8. Collections](#8-collections)
- [9. Date and Time](#9-date-and-time)
- [10. Strings](#10-strings)

## 1. Naming Conventions

Follow the Java naming conventions.

- Package names should be all lowercase, without underscores or other special characters. They
  should be short, meaningful, and based on the project's domain.
- Class and interface names should be in `PascalCase` and should be nouns or noun phrases.
- Method names should be in `camelCase` and should be verbs or verb phrases.
- Variable names should be in `camelCase` and should be short and meaningful. Avoid single-letter
  variable names except for loop counters.
- Constant names should be in `UPPER_SNAKE_CASE`.

## 2. Code Layout

- Use 4 spaces for indentation. Do not use tabs.
- Consistent indentation is crucial for readability. Using spaces instead of tabs ensures that the
  code looks the same on all systems.
- Keep lines of code under 120 characters.
- When wrapping lines, break after a comma or an operator. Indent the new line with 8 spaces.

## 3. Best Practices for Classes, Interfaces, and Enums

### 3.1. Use Records for data holder classes

- **Guideline:** Prefer using Java records for storing data holder classes.

- **Example:**

  ```java
  // Good
  public record CustomerDTO(String name, String email) { }
  
  // Bad
  public class CustomerDTO {
      private String name;
      private String email;

      // Getters and setters
  }
  ```

### 3.2. Immutability

- **Guideline:** Prefer immutable classes whenever possible.

- **Example:**

  ```java
  // Good
  public final class Customer {
      private final String name;
      private final String email;

      public Customer(String name, String email) {
          this.name = name;
          this.email = email;
      }

      // Getters only
  }

  // Bad
  public class Customer {
      private String name;
      private String email;

      // Getters and setters
  }
  ```

- **Explanation:** Immutable objects are inherently thread-safe and make the code easier to reason
  about.

### 3.3. Use Interfaces

- **Guideline:** Program to interfaces, not implementations.

- **Example:**

  ```java
  // Good
  List<String> names = new ArrayList<>();

  // Bad
  ArrayList<String> names = new ArrayList<>();
  ```

- **Explanation:** This makes the code more flexible and easier to test, as you can easily swap out
  implementations.

### 3.4. Use Enums

- **Guideline:** Use enums instead of string constants or integer constants.

- **Example:**

  ```java
  // Good
  public enum Status {
      PENDING,
      ACTIVE,
      INACTIVE
  }

  // Bad
  public static final String STATUS_PENDING = "PENDING";
  public static final int STATUS_ACTIVE = 1;
  ```

- **Explanation:** Enums are type-safe and provide more readable and maintainable code.

## 4. Exception Handling

### 4.1. Specific Exceptions

- **Guideline:** Catch specific exceptions instead of `Exception` or `Throwable`.

- **Example:**

  ```java
  // Good
  try {
      // ...
  } catch (IOException e) {
      // ...
  }

  // Bad
  try {
      // ...
  } catch (Exception e) {
      // ...
  }
  ```

- **Explanation:** Catching specific exceptions makes the error handling more robust and prevents
  catching unexpected exceptions.

### 4.2. Don't Ignore Exceptions

- **Guideline:** Never ignore exceptions. If you catch an exception, either handle it or rethrow it.

- **Example:**

  ```java
  // Good
  try {
      // ...
  } catch (IOException e) {
      log.error("Failed to read file", e);
  }

  // Bad
  try {
      // ...
  } catch (IOException e) {
      // ignored
  }
  ```

- **Explanation:** Ignoring exceptions can lead to subtle bugs and make the code harder to debug.

## 5. Concurrency

### 5.1. Use `java.util.concurrent`

- **Guideline:** Prefer the high-level concurrency utilities in the `java.util.concurrent` package
  over low-level primitives like `wait()` and `notify()`.

- **Example:**

  ```java
  // Good
  ExecutorService executor = Executors.newFixedThreadPool(10);
  executor.submit(() -> {
      // ...
  });

  // Bad
  new Thread(() -> {
      // ...
  }).start();
  ```

- **Explanation:** The `java.util.concurrent` package provides a rich set of tools that are more
  robust and easier to use than the low-level concurrency primitives.

### 5.2. Avoid `volatile` for Complex Operations

- **Guideline:** Use `volatile` only for simple atomic operations. For more complex operations, use
  `java.util.concurrent.atomic` or locks.

- **Explanation:** `volatile` ensures visibility but not atomicity for compound actions. Using it
  incorrectly can lead to subtle concurrency bugs.

## 6. Use of `Optional`

### 6.1. `Optional` for Return Types

- **Guideline:** Use `Optional` for return types when a method might not return a value.

- **Example:**

  ```java
  // Good
  public Optional<Customer> findCustomerById(Long id) {
      // ...
  }

  // Bad
  public Customer findCustomerById(Long id) {
      // returns null if not found
  }
  ```

- **Explanation:** Using `Optional` makes the API clearer and helps prevent `NullPointerException`.

### 6.2. Don't Use `Optional` for Fields or Parameters

- **Guideline:** Do not use `Optional` for class fields or method parameters.

- **Explanation:** This can make the code more complex and less readable. For optional dependencies,
  use method overloading or a nullable annotation.

## 7. Stream API Best Practices

### 7.1. Avoid Side Effects

- **Guideline:** Avoid side effects in stream operations like `map()` and `filter()`.

- **Example:**

  ```java
  // Good
  List<String> names = customers.stream()
      .map(Customer::getName)
      .collect(Collectors.toList());

  // Bad
  List<String> names = new ArrayList<>();
  customers.stream()
      .forEach(c -> names.add(c.getName()));
  ```

- **Explanation:** Side effects in stream operations can lead to unpredictable behavior, especially
  in parallel streams.

### 7.2. Prefer Method References

- **Guideline:** Prefer method references over lambdas when possible.

- **Example:**

  ```java
  // Good
  List<String> names = customers.stream()
      .map(Customer::getName)
      .collect(Collectors.toList());

  // Bad
  List<String> names = customers.stream()
      .map(c -> c.getName())
      .toList();
  ```

- **Explanation:** Method references are more concise and readable.

## 8. Collections

### 8.1. Use the Right Collection

- **Guideline:** Choose the right collection for the job. Use `List` for ordered collections, `Set`
  for unordered collections with no duplicates, and `Map` for key-value pairs.
- **Explanation:** Using the appropriate collection type improves performance and makes the code's
  intent clearer. For example, using a `Set` when you need to ensure uniqueness is more efficient
  than checking for duplicates in a `List` manually.

### 8.2. Prefer `isEmpty()` over `size() == 0`

- **Guideline:** Use `isEmpty()` to check if a collection is empty.
- **Example:**
  ```java
  // Good
  if (names.isEmpty()) { ... }

  // Bad
  if (names.size() == 0) { ... }
  ```
- **Explanation:** `isEmpty()` is more readable and can be more performant for some collection
  types, as it may not need to count all the elements.

### 8.3. Return Empty Collections, Not Null

- **Guideline:** Methods that return collections should return an empty collection instead of
  `null`.
- **Example:**
  ```java
  // Good
  public List<String> getNames() {
      if ( ... ) {
          return Collections.emptyList();
      }
      // ...
  }

  // Bad
  public List<String> getNames() {
      if ( ... ) {
          return null;
      }
      // ...
  }
  ```
- **Explanation:** This prevents `NullPointerException`s in the calling code and simplifies it, as
  the caller doesn't need to handle a `null` case.

### 8.4. Use Diamond Operator

- **Guideline:** Use the diamond operator (`<>`) for generic type inference.
- **Example:**
  ```java
  // Good
  List<String> names = new ArrayList<>();

  // Bad
  List<String> names = new ArrayList<String>();
  ```
- **Explanation:** The diamond operator reduces boilerplate code and improves readability without
  sacrificing type safety.

### 8.5. Use `for-each` loop

- **Guideline:** Prefer the `for-each` loop for iterating over collections.
- **Example:**
  ```java
  // Good
  for (String name : names) { ... }

  // Bad
  for (int i = 0; i < names.size(); i++) { ... }
  ```
- **Explanation:** The `for-each` loop is more concise, less error-prone (no off-by-one errors), and
  more readable. Use an iterator or a traditional for loop only when you need to modify the
  collection while iterating.

## 9. Date and Time

### 9.1. Prefer Java 8 Date-Time API

- **Guideline:** Prefer using the Java 8 Date-Time API (`java.time.*`) over legacy `java.util.Date`
  and `java.util.Calendar`.

- **Example:**

  ```java
  // Good
  LocalDate birthday = LocalDate.of(1990, Month.MAY, 12);
  LocalDate today = LocalDate.now(ZoneId.of("UTC"));
  Period age = Period.between(birthday, today);

  ZonedDateTime meeting = ZonedDateTime.of(2026, 2, 3, 9, 30, 0, 0, ZoneId.of("Europe/Berlin"));
  String iso = meeting.format(DateTimeFormatter.ISO_ZONED_DATE_TIME);

  // Bad
  Date date = new Date();
  Calendar cal = Calendar.getInstance();
  cal.set(1990, Calendar.MAY, 12);
  ```

- **Explanation:** The `java.time` API is immutable, thread-safe, and more expressive. It offers
  clear types for different concepts (`Instant`, `LocalDate`, `LocalDateTime`, `ZonedDateTime`,
  `Duration`, `Period`) and comprehensive formatting/parsing with `DateTimeFormatter`.

## 10. Strings

### 10.1. Use Multiline Text Blocks for Multi-line Strings

- **Guideline:** Use text blocks (`"""`), available since Java 15, for multi-line string literals (
  e.g., SQL, JSON, XML) instead of concatenation or `\n` escapes.

- **Example:**

  ```java
  // Good
  String sql = """
      SELECT id, name
      FROM customers
      WHERE status = 'ACTIVE'
      ORDER BY name
      """;

  String json = """
      {
        "name": "Alice",
        "roles": ["USER", "ADMIN"]
      }
      """;

  // Bad
  String sqlBad = "SELECT id, name\n" +
                  "FROM customers\n" +
                  "WHERE status = 'ACTIVE'\n" +
                  "ORDER BY name";
  ```

- **Explanation:** Text blocks improve readability, reduce escaping, and preserve intended
  formatting. They also support strip indentation and trailing newline control, making them ideal
  for embedded resources and templates.

# Spring Boot Guidelines

## 1. Prefer Constructor Injection over Field/Setter Injection

* Declare all the mandatory dependencies as `final` fields and inject them through the constructor.
* Spring will auto-detect if there is only one constructor, no need to add `@Autowired` on the
  constructor.
* Avoid field/setter injection in production code.

**Explanation:**

* Making all the required dependencies as `final` fields and injecting them through constructor make
  sure that the object is always in a properly initialized state using the plain Java language
  feature itself. No need to rely on any framework-specific initialization mechanism.
* You can write unit tests without relying on reflection-based initialization or mocking.
* The constructor-based injection clearly communicates what are the dependencies of a class without
  having to look into the source code.
* Spring Boot provides extension points as builders such as `RestClient.Builder`,
  `ChatClient.Builder`, etc. Using constructor-injection, we can do the customization and initialize
  the actual dependency.

```java

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final RestClient restClient;

  public OrderService(OrderRepository orderRepository,
      RestClient.Builder builder) {
    this.orderRepository = orderRepository;
    this.restClient = builder
        .baseUrl("http://catalog-service.com")
        .requestInterceptor(new ClientCredentialTokenInterceptor())
        .build();
  }

  //... methods
}
```

## 2. Prefer package-private over public for Spring components

* Declare Controllers, their request-handling methods, `@Configuration` classes and `@Bean` methods
  with default (package-private) visibility whenever possible. There's no obligation to make
  everything `public`.

**Explanation:**

* Keeping classes and methods package-private reinforces encapsulation and abstraction by hiding
  implementation details from the rest of your application.
* Spring Boot's classpath scanning will still detect and invoke package-private components (for
  example, invoking your `@Bean` methods or controller handlers), so you can safely restrict
  visibility to only what clients truly need. This approach confines your internal APIs to a single
  package while still allowing the framework to wire up beans and handle HTTP requests.

## 3. Organize Configuration with Typed Properties

* Group application-specific configuration properties with a common prefix in
  `application.properties` or `.yml`.
* Bind them to `@ConfigurationProperties` classes with validation annotations so that the
  application will fail fast if the configuration is invalid.
* Prefer environment variables instead of profiles for passing different configuration properties
  for different environments.

**Explanation:**

* By grouping and binding configuration in a single `@ConfigurationProperties` bean, you centralize
  both the property names and their validation rules.
  In contrast, using `@Value("${…}")` across many components forces you to update each injection
  point whenever a key or validation requirement changes.
* Overusing profiles to customize the application configuration may lead to unexpected issues due to
  the order of profiles specified.
  As you can enable multiple profiles with different combinations, making sense of the effective
  application configuration becomes tricky.

## 4. Define Clear Transaction Boundaries

* Define each Service-layer method as a transactional unit.
* Annotate query-only methods with `@Transactional(readOnly = true)`.
* Annotate data-modifying methods with `@Transactional`.
* Limit the code inside each transaction to the smallest necessary scope.

**Explanation:**

* **Single Unit of Work:** Group all database operations for a given use case into one atomic unit,
  which in Spring Boot is typically a `@Service` annotated class method. This ensures that either
  all operations succeed or none do.
* **Connection Reuse:** A `@Transactional` method runs on a single database connection for its
  entire scope, avoiding the overhead of acquiring and returning connections from the connection
  pool for each operation.
* **Read-only Optimizations:** Marking methods as `readOnly = true` disables unnecessary
  dirty-checking and flushes, improving performance for pure reads.
* **Reduced Contention:** Keeping transactions as brief as possible minimizes lock duration,
  lowering the chance of contention in high-traffic applications.

## 5. Disable Open Session in View Pattern

* While using Spring Data JPA, disable the Open Session in View filter by setting
  ` spring.jpa.open-in-view=false` in `application.properties/yml.`

**Explanation:**

* Open Session In View (OSIV) filter transparently enables loading the lazy associations while
  rendering the view or serializing JPA entities. This may lead to the N + 1 Select problem.
* Disabling OSIV forces you to fetch exactly the associations you need via fetch joins, entity
  graphs, or explicit queries, and hence you can avoid unexpected N + 1 selects and
  `LazyInitializationExceptions`.

## 6. Separate Web Layer from Persistence Layer

* Don't expose entities directly as responses in controllers.
* Define explicit request and response record (DTO) classes instead.
* Apply Jakarta Validation annotations on your request records to enforce input rules.

**Explanation:**

* Returning or binding directly to entities couples your public API to your database schema, making
  future changes riskier.
* DTOs let you clearly declare exactly which fields clients can send or receive, improving clarity
  and security.
* With dedicated DTOs per use case, you can annotate fields for validation without relying on
  complex validation groups.
* Use Java bean mapper libraries to simplify DTO conversions. Prefer MapStruct library that can
  generate bean mapper implementation at compile time so that there won't be runtime reflection
  overhead.

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

**Explanation:**

* **Predictability and discoverability:** Adhering to well-known REST conventions makes your API
  intuitive. Clients can guess URLs and behaviors without extensive documentation.
* **Reliable client integrations:** Standardized URL structures, status codes, and headers enable
  consumers to build against your API with confidence, knowing exactly what each response will look
  like.
* For more comprehensive REST API Guidelines, please
  refer [Zalando RESTful API and Event Guidelines](https://opensource.zalando.com/restful-api-guidelines/).

## 8. Use Command Objects for Business Operations

* Create purpose-built command records (e.g., `CreateOrderCommand`) to wrap input data.
* Accept these commands in your service methods to drive creation or update workflows.

**Explanation:**

* Using the use-case specific Command and Query objects clearly communicates what input data is
  expected from the caller.
  Otherwise, the caller had to guess whether they should create and pass the unique key or
  created_date, or they will be generated by the server/database.

## 9. Centralize Exception Handling

* Define a global handler class annotated with `@ControllerAdvice` (or `@RestControllerAdvice` for
  REST APIs) using `@ExceptionHandler` methods to handle specific exceptions.
* Return consistent error responses. Consider using the ProblemDetails response
  format ([RFC 9457](https://www.rfc-editor.org/rfc/rfc9457)).

**Explanation:**

* We should always handle all possible exceptions and return a standard error response instead of
  throwing exceptions.
* It is better to centralize the exception handling in a `GlobalExceptionHandler` using
  `(Rest)ControllerAdvice` instead of duplicating the try/catch exception handling logic across the
  controllers.

## 10. Actuator

* Expose only essential actuator endpoints (such as `/health`, `/info`, `/metrics`) without
  requiring authentication. All the other actuator endpoints must be secured.

**Explanation:**

* Endpoints like `/actuator/health` and `/actuator/metrics` are critical for external health checks
  and metric collection (e.g., by Prometheus). Allowing these to be accessed anonymously ensures
  monitoring tools can function without extra credentials. All the remaining endpoints should be
  secured.
* In non-production environments (DEV, QA), you can expose additional actuator endpoints such as
  `/actuator/beans`, `/actuator/loggers` for debugging purpose.

## 11. Internationalization with ResourceBundles

* Externalize all user-facing text such as labels, prompts, and messages into ResourceBundles rather
  than embedding them in code.

**Explanation:**

* Hardcoded strings make it difficult to support multiple languages. By placing your labels, error
  messages, and other text in locale-specific ResourceBundle files, you can maintain separate
  translations for each language.
* At runtime, Spring can load the appropriate bundle based on the user's locale or a preference
  setting, making it simple to add new languages and switch between them dynamically.

## 12. Use Testcontainers for integration tests

* Spin up real services (databases, message brokers, etc.) in your integration tests to mirror
  production environments.

**Explanation:**

* Most of the modern applications use a wide range of technologies such as SQL/NoSQL databases,
  key-value stores, message brokers, etc. Instead of using in-memory variants or mocks,
  Testcontainers can spin up those dependencies as Docker containers and allow you to test using the
  same type of dependencies that you will use in the production. This reduces environment
  inconsistencies and increases confidence in your integration tests.
* Always use docker images with a specific version of the dependency that you are using in
  production instead of using the `latest` tag.

## 13. Use random port for integration tests

* When writing integration tests, start the application on a random available port to avoid port
  conflicts by annotating the test class with:

    ```java
    @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    ```

**Explanation:**

* **Avoid conflicts in CI/CD:** In your CI/CD environment, there can be multiple builds running in
  parallel on the same server/agent. In such cases, it is better to run the integration tests using
  a random available port rather than a fixed port to avoid port conflicts.

## 14. Logging

* **Use a proper logging framework.**  
  Never use `System.out.println()` for application logging. Rely on SLF4J (or a compatible
  abstraction) and your chosen backend (Logback, Log4j2, etc.).

* **Protect sensitive data.**  
  Ensure that no credentials, personal information, or other confidential details ever appear in log
  output.

* **Guard expensive log calls.**  
  When building verbose messages at `DEBUG` or `TRACE` level, especially those involving method
  calls or complex string concatenations, wrap them in a level check or use suppliers:

```java
if(logger.isDebugEnabled()){
    logger.

debug("Detailed state: {}",computeExpensiveDetails());
    }

    // using Supplier/Lambda expression
    logger
    .

atDebug()
        .

setMessage("Detailed state: {}")
        .

addArgument(() ->

computeExpensiveDetails())
    .

log();
```

**Explanation:**

* **Flexible verbosity control:** A logging framework lets you adjust what gets logged and where
  with the support for tuning log levels per environment (development, testing, production).

* **Rich contextual metadata:** Beyond the message itself, you can capture class/method names,
  thread IDs, process IDs, and any custom context via MDC, aiding diagnosis.

* **Multiple outputs and formats:** Direct logs to consoles, rolling files, databases, or remote
  systems, and choose formats like JSON for seamless ingestion into ELK, Loki, or other log-analysis
  tools.

* **Better tooling and analysis:** Structured logs and controlled log levels make it easier to
  filter noise, automate alerts, and visualize application behavior in real time.

# Next.js 16 Project Guidelines

<!-- BEGIN:nextjs-16-agent-rules -->

- **Bundler**: Turbopack is the default. Do not use Webpack-specific configurations unless
  explicitly required via the `--webpack` flag.
- **Request APIs**: Always `await` for `params`, `searchParams`, `cookies()`, `headers()`, and
  `draftMode()`. They are now strictly asynchronous.
- **Network Boundaries**: Use `proxy.ts` instead of `middleware.ts` for request interception and
  redirects. Ensure the exported function is named `proxy`.
- **Caching Model**: Use the "Cache Components" model with the `"use cache"` directive for granular
  server-side caching.
- **React Compiler**: React Compiler is stable. Avoid manual `useMemo` and `useCallback`
  optimizations; let the compiler handle memoization automatically.
- **Advanced Routing**: Leverage layout deduplication and incremental prefetching. Next.js 16
  automatically optimizes shared layouts.

<!-- END:nextjs-16-agent-rules -->

## 🛠 Next.js 16 Commands

| Task                | Command                                   |
|:--------------------|:------------------------------------------|
| Development (Turbo) | `next dev` (Turbopack is now default)     |
| Production Build    | `next build`                              |
| Automated Upgrade   | `npx @next/codemod@canary upgrade latest` |
| Clean Build Cache   | `rm -rf .next`                            |

## 🏗 Key Architectural Changes

- **`proxy.ts`**: Replaces `middleware.ts`. It runs on the Node.js runtime by default to clarify the
  network boundary between external requests and the app.
- **Async Runtime**: Node.js 20.9+ is the minimum requirement. Ensure all environment-specific code
  is compatible.
- **Parallel Routes**: All parallel route slots now require an explicit `default.js` file; builds
  will fail without them.
- **Caching APIs**:
    - Use `updateTag()` in Server Actions for "read-your-writes" (immediate) updates.
    - Use `revalidateTag(tag, 'max')` for background stale-while-revalidate behavior.
    - Use `refresh()` for updating uncached dynamic data like notification counts.

## 🖼 Image & Security

- **Image Security**: `images.dangerouslyAllowLocalIP` is now `false` by default. Do not enable
  unless on a private network.
- **Strict Config**: Next.js 16 enforces stricter TypeScript checking for `next.config.ts`. Always
  use `import type { NextConfig } from 'next'`.

## 🧪 Testing & Debugging

- **DevTools MCP**: Junie can use the Model Context Protocol (MCP) to inspect routing, cache
  behavior, and build metrics directly.
- **Logging**: Use the improved build and development logs to identify compilation bottlenecks.

# Javascript Guidelines

You are an expert in JavaScript, Next.js, TypeScript, and scalable web application
development. You write secure, maintainable, and performant code following Next.js and JavaScript
best practices.

## JavaScript Best Practices

- Follow Biome configurations
- Use ES6+ features (arrow functions, destructuring, etc.)
- Prefer const over let, avoid var
- Use async/await for asynchronous operations
- Use template literals for string concatenation

**Explanation:**

- Biome enforces consistent code style and catch potential issues early
- Modern JavaScript features like arrow functions, destructuring, and spread operators make code
  more concise and readable
- Using const by default prevents accidental reassignment and makes code intentions clearer
- Async/await provides cleaner syntax for handling promises compared to .then() chains
- Template literals (`${variable}`) are more readable than string concatenation with + operators

## Components

- Create reusable components in the components directory
- Use TypeScript for props
- Use script setup (with TS by default)
- Use props destructuring
- Implement proper component naming (PascalCase)
- Use slots for flexible component content
- Organize components in subdirectories by feature

**Explanation:**

- TypeScript for props provides better type safety, autocompletion, and documentation
- Script setup with TypeScript provides a more concise syntax and better type inference
- Props destructuring with default values is cleaner than using withDefaults
- PascalCase naming distinguishes components from HTML elements and follows Vue conventions
- Slots allow components to be more flexible and reusable across different contexts
- Organizing by feature (e.g., /components/auth/, /components/products/) improves discoverability

## State Management

- prefer useState when possible
- use Redux for more complex state management
- Avoid global state when component or page-level state is sufficient
- Do not use ref for global state
- Structure stores by domain/feature
- Implement proper typing for state

**Explanation:**

- Redux is the recommended store for complex applications, offering better TypeScript support and
  devtools integration
- Overusing global state makes applications harder to test and reason about
- Using ref for global state can lead to reactivity issues across components and during SSR
- Domain-based store organization (e.g., user store, product store) improves maintainability
- TypeScript typing for state prevents errors and improves developer experience

## TypeScript

- Use TypeScript for better type safety
- Define interfaces and types for data structures
- Use generics when appropriate
- Leverage auto-imports for types
- Avoid using "any" type
- write erasableSyntaxOnly compliant code only (no enums, namespaces, and class parameter
  properties)

**Explanation:**

- TypeScript catches type-related errors at compile time rather than runtime
- Well-defined interfaces document the shape of your data and improve code quality
- Generics allow for reusable components and functions that work with different types
- Using "any" defeats the purpose of TypeScript and should be avoided when possible
- erasableSyntaxOnly compliant code ensures better compatibility with JavaScript and avoids
  TypeScript-only features that don't have direct JavaScript equivalents

## Performance

- Implement proper code-splitting
- Use lazy loading for components when appropriate
- Optimize images with Next JS Image
- Implement proper caching strategies
- Use server components for data-heavy operations

**Explanation:**

- Code-splitting reduces initial bundle size by only loading code when needed
- Lazy loading components improves initial page load times
- Next Image automatically optimizes images with proper sizing, formats, and lazy loading
- Caching strategies like stale-while-revalidate improve perceived performance
- Server components in Next allow heavy data processing to happen on the server, sending only the
  result to the client

## SEO

- Use generateMetadata for page-level metadata
- Use semantic HTML elements
- Ensure accessibility compliance

## Testing

- Write unit tests for components
- Implement end-to-end tests for critical user flows
- Test both positive and negative scenarios
- Use vitest + react testing library

**Explanation:**

- Unit tests verify that individual components and composables work correctly in isolation
- End-to-end tests ensure that critical user flows work correctly from the user's perspective
- Testing negative scenarios (errors, edge cases) is as important as testing the happy path

# API Guidelines

- Define APIs using OpenAPI 3.1.1 [specification](https://spec.openapis.org/oas/v3.1.1).
- Share the same spec file between frontend and backend.
- Autogenerate code from the spec file.
- Maintain separate files for each distinct API.
- Include auth details in the spec file.
