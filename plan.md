# plan.md — Spring Boot qualifier: full implementation plan (ready for GitHub Copilot)

**Purpose:** a complete, step-by-step plan that you can feed to GitHub Copilot (or follow yourself) to build the Spring Boot app required by the question paper: on startup generate a webhook, decide which SQL to submit (based on regNo last two digits), store the result, and send the final SQL to the returned webhook using the provided JWT.

---

## Quick summary

* App type: Spring Boot (no controller entrypoints) — run flow on startup.
* Java: 17+, Build: Maven
* HTTP client: `RestTemplate` **or** `WebClient` (we'll use `RestTemplate` in examples).
* Output: `finalQuery.sql` (file) and submit JSON `{ "finalQuery": "..." }` to webhook with `Authorization: <accessToken>`.
* Handle odd/even last two digits of `regNo`: Odd → Question 1 (SQL provided). Even → Question 2 (placeholder; fetch or implement Q2 SQL in repo).

---

## Prerequisites (for local dev / Copilot prompts)

* JDK 17
* Maven
* Git
* (optional) Docker for running mock servers during tests

---

## Deliverables (what your GitHub repo must contain)

1. Source code (Java + resources)
2. `finalQuery.sql` produced at runtime
3. `pom.xml` and `README.md`
4. Final JAR file (in repo releases or raw downloadable link)
5. `plan.md` (this file)

---

## Recommended repo structure

```
bfh-java-qualifier/
├─ README.md
├─ plan.md
├─ pom.xml
├─ src/
│  ├─ main/
│  │  ├─ java/com/bfh/qualifier/
│  │  │  ├─ App.java
│  │  │  ├─ service/WebhookService.java
│  │  │  ├─ dto/GenerateResponse.java
│  │  │  ├─ dto/SubmitResponse.java
│  │  │  ├─ util/SQLProvider.java
│  │  └─ resources/
│  │     ├─ application.properties
│  │     └─ finalQuery.sql (generated at runtime)
│  └─ test/
│     └─ java/... (unit tests)
└─ target/
```

---

## Step-by-step implementation plan (detailed)

### 0) Create `pom.xml`

**Copilot prompt:** *"Create a Maven `pom.xml` for a Spring Boot 3.x project using Java 17. Include dependencies: spring-boot-starter, spring-boot-starter-web, spring-boot-starter-test (scope test), jackson (already included), and optional h2 for local persistence. Configure spring-boot-maven-plugin for packaging jar."*

**Notes:** keep versions aligned with Spring Boot BOM. No extra heavy dependencies needed.

---

### 1) `application.properties` (configuration)

Create `src/main/resources/application.properties` with keys used by the app:

```
bfh.name=YOUR_NAME
bfh.regNo=YOUR_REG_NO
bfh.email=your.email@example.com
# timeouts for RestTemplate
bfh.http.connectTimeoutMs=5000
bfh.http.readTimeoutMs=15000
# fallback submission endpoint (from paper)
bfh.fallbackUrl=https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA
```

Copilot should generate `@ConfigurationProperties` or use `@Value` injection for these properties.

---

### 2) `SQLProvider.java` — SQL constants

Create a small class with `public static final String SQL_Q1` (the provided final SQL for Question 1) and `SQL_Q2` as placeholder.

**SQL\_Q1 (MySQL):**

```sql
SELECT
    p.AMOUNT AS SALARY,
    CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME,
    TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE,
    d.DEPARTMENT_NAME
FROM PAYMENTS p
JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
WHERE DAY(p.PAYMENT_TIME) <> 1
ORDER BY p.AMOUNT DESC
LIMIT 1;
```

**SQL\_Q2:**

* Leave a `"TODO: add Q2 SQL here"` placeholder or add a generic SELECT so the app still runs for even regNo during tests.

---

### 3) `GenerateResponse` DTO

Create a simple DTO matching the expected generateWebhook response fields:

```java
public class GenerateResponse {
    private String webhook; // full URL to POST finalQuery
    private String accessToken; // JWT token to use in Authorization header
    // getters/setters
}
```

Also prepare a `SubmitResponse` DTO (string body) or just accept `String` response.

---

### 4) `WebhookService.java` — HTTP operations

Create a service that encapsulates: `generateWebhook()` and `submitFinalQuery(webhookUrl, token, query)`.

**Implementation details:**

* Use `RestTemplate` bean configured with timeouts from properties.
* `generateWebhook()` should `POST` JSON `{ "name": ..., "regNo": ..., "email": ... }` to `https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA` and map to `GenerateResponse`.
* `submitFinalQuery(...)` should prepare headers:

  * `Content-Type: application/json`
  * `Authorization: <accessToken>` (exactly the token string)
  * Body: `{ "finalQuery": "<SQL string>" }`
* If `submitFinalQuery` to the returned webhook fails (non-2xx or exception), call fallback URL (from properties).
* Log requests/responses (but do NOT log sensitive token to public logs in real product — for this exercise printing is OK).

**Copilot prompt:** *"Create a WebhookService with methods generateWebhook() and submitFinalQuery(). Use RestTemplate and map responses to DTOs. Provide retries or fallback to `bfh.fallbackUrl` on failure."*

---

### 5) `App.java` (main startup flow)

**Behavior (exact steps Copilot should implement):**

1. Read `bfh.name`, `bfh.regNo`, `bfh.email` from `application.properties` or command line args.
2. On application startup (implement `CommandLineRunner` or `ApplicationRunner`), call `WebhookService.generateWebhook()`.
3. Parse returned `GenerateResponse` to get `webhook` and `accessToken`. If null/invalid → log and exit non-zero.
4. Decide which SQL to use:

   * Extract last two digits: `String lastTwo = regNo.replaceAll("[^0-9]", ""); lastTwo = lastTwo.length()>=2 ? lastTwo.substring(lastTwo.length()-2) : lastTwo; int dd = Integer.parseInt(lastTwo);`
   * If `dd % 2 == 1` → `SQL_Q1`, else → `SQL_Q2`.
   * (If `SQL_Q2` is TODO placeholder, app should still save that string and attempt submission.)
5. Save SQL string to two places:

   * `src/main/resources/finalQuery.sql` (so it appears in artifact) **and**
   * Root project file `finalQuery.sql` next to jar (write to working dir).
6. Call `WebhookService.submitFinalQuery(webhook, accessToken, finalQuery)`.
7. Log submission response and exit (System.exit(0)).

**Extra:** include an option `--bfh.skipExit=false` to prevent System.exit for local debugging.

---

### 6) Error handling & logging

* If `generateWebhook()` returns 4xx/5xx or invalid JSON, print error and exit.
* If submission fails to webhook, attempt fallback URL and save the response for debugging.
* Wrap network calls in try/catch with informative messages.

---

### 7) Tests

* Unit test `WebhookService` with `MockRestServiceServer` or `MockRestTemplate`.

  * Mock generateWebhook response JSON and assert parsed `GenerateResponse`.
  * Mock webhook submit endpoint and return 200 — assert service handles it.
* Integration test for `App` using `@SpringBootTest` but mock network endpoints.

**Copilot prompt:** *"Write unit tests for WebhookService that mock HTTP responses using MockRestServiceServer."*

---

### 8) Build, package & produce final jar (CI / manual)

Commands:

```bash
# build
mvn -DskipTests clean package

# run
java -jar target/bfh-java-qualifier-1.0.0.jar --bfh.name="John Doe" --bfh.regNo="REG12347" --bfh.email="john@example.com"
```

**CI idea:** Add a GitHub Actions workflow `.github/workflows/maven.yml` that:

* Checks out
* Sets up JDK 17
* Runs `mvn -DskipTests package`
* Uploads the jar as an artifact and optionally creates a release

When the jar is uploaded to GitHub Releases, a raw download link will act as the public JAR link required by the submission checklist.

---

### 9) README.md — content to include

* Short description and how the app fulfills the assignment
* Steps to build & run
* Sample `application.properties` placeholders
* Where `finalQuery.sql` will be saved
* How to reproduce submission (example run)
* Link to the final jar (once uploaded)

---

### 10) Submission checklist (what to upload to the forms.office URL)

* Public GitHub repo URL (e.g. `https://github.com/your-username/bfh-java-qualifier`)
* Code (all source)
* Final JAR (release or raw link)
* `finalQuery.sql` present in repository after a successful run (include generated file in repo or in release assets)

---

## Copilot-ready prompts (copy-paste)

Below are short prompts you can paste to GitHub Copilot to generate each file quickly.

1. **pom.xml** prompt:

> "Create a Maven `pom.xml` for a Spring Boot 3.x project using Java 17. Include dependencies: spring-boot-starter, spring-boot-starter-web, spring-boot-starter-test (test scope), and H2 optional. Configure maven plugin to build fat jar."

2. **App.java** prompt:

> "Create a Spring Boot `App` class that implements `CommandLineRunner`. On startup it should read `bfh.name`, `bfh.regNo`, `bfh.email` from `application.properties`, call a WebhookService.generateWebhook(), determine last two digits of regNo (only digits), pick SQL\_Q1 if odd else SQL\_Q2, write SQL to `finalQuery.sql`, and call WebhookService.submitFinalQuery(webhook, accessToken, finalQuery). Add clean logging and System.exit(0) at end."

3. **WebhookService.java** prompt:

> "Create a service `WebhookService` with a RestTemplate. Add method `GenerateResponse generateWebhook(String name, String regNo, String email)` that POSTs JSON to `https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA` and maps response. Add method `String submitFinalQuery(String webhookUrl, String token, String finalQuery)` which sends `{ finalQuery: '...' }` with header `Authorization: token`. Provide fallback URL injection."

4. **SQLProvider.java** prompt:

> "Create a util class `SQLProvider` with two public static Strings: `SQL_Q1` (the exact SQL for Question 1) and `SQL_Q2` placeholder text."

5. **Unit test** prompt:

> "Write a unit test for WebhookService that uses MockRestServiceServer to mock generateWebhook response and submission endpoint. Assert that generateWebhook returns expected DTO and that submitFinalQuery successfully posts JSON and handles 200 OK."

