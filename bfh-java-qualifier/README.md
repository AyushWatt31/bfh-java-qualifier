# BFH Java Qualifier

This is a Spring Boot application for the BFH Java Qualifier assignment. It generates a webhook, selects an SQL query based on your registration number, saves the query, and submits it to the webhook using a JWT token.

## How it works
- On startup, reads your name, regNo, and email from `application.properties`.
- Calls the webhook generator endpoint and receives a webhook URL and JWT token.
- Determines which SQL to use (odd/even last two digits of regNo).
- Saves the SQL to `finalQuery.sql` in both the project root and `src/main/resources`.
- Submits the SQL to the webhook with the JWT token.
- Falls back to a default endpoint if submission fails.

## Build & Run

```bash
mvn -DskipTests clean package
java -jar target/bfh-java-qualifier-1.0.0.jar --bfh.name="John Doe" --bfh.regNo="REG12347" --bfh.email="john@example.com"
```

## Configuration
Edit `src/main/resources/application.properties`:
```
bfh.name=YOUR_NAME
bfh.regNo=YOUR_REG_NO
bfh.email=your.email@example.com
bfh.http.connectTimeoutMs=5000
bfh.http.readTimeoutMs=15000
bfh.fallbackUrl=https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA
```

## Output
- `finalQuery.sql` will be saved in the project root and in `src/main/resources` after running.

## Submission
- Upload your public GitHub repo URL, final JAR, and generated `finalQuery.sql` as required.
