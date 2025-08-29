# BFH Java Qualifier

This repository contains a Spring Boot application for the BFH Java Qualifier assignment. The app generates a webhook, selects an SQL query based on your registration number, saves the query, and submits it to the webhook using a JWT token.

## Features
- Reads your name, registration number, and email from `application.properties` or command line arguments
- Calls the webhook generator endpoint and receives a webhook URL and JWT token
- Determines which SQL to use (odd/even last two digits of regNo)
- Saves the SQL to `finalQuery.sql` in both the project root and `src/main/resources`
- Submits the SQL to the webhook with the JWT token
- Falls back to a default endpoint if submission fails

## Project Structure
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

## How to Build & Run

1. **Install JDK 17 and Maven**
2. **Configure your details in `src/main/resources/application.properties`:**
	```
	bfh.name=YOUR_NAME
	bfh.regNo=YOUR_REG_NO
	bfh.email=your.email@example.com
	bfh.http.connectTimeoutMs=5000
	bfh.http.readTimeoutMs=15000
	bfh.fallbackUrl=https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA
	```
3. **Build the project:**
	```
	mvn -DskipTests clean package
	```
4. **Run the application:**
	```
	java -jar target/bfh-java-qualifier-1.0.0.jar --bfh.name="John Doe" --bfh.regNo="REG12347" --bfh.email="john@example.com"
	```

## How to Test

Run all unit and integration tests:
```
mvn test
```
Test results will be shown in the terminal and saved in `target/surefire-reports/`.

## Output
- `finalQuery.sql` will be saved in the project root and in `src/main/resources` after running.

## Submission Checklist
- Public GitHub repo URL
- Source code
- Final JAR (release or raw link)
- `finalQuery.sql` present in repository after a successful run

## License
This project is for educational purposes.
