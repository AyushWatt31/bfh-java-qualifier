package com.bfh.qualifier;

import com.bfh.qualifier.service.WebhookService;
import com.bfh.qualifier.dto.GenerateResponse;
import com.bfh.qualifier.util.SQLProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.FileWriter;
import java.io.IOException;

@SpringBootApplication
public class App implements CommandLineRunner {
    @Value("${bfh.name}")
    private String name;
    @Value("${bfh.regNo}")
    private String regNo;
    @Value("${bfh.email}")
    private String email;
    @Value("${bfh.http.connectTimeoutMs}")
    private int connectTimeout;
    @Value("${bfh.http.readTimeoutMs}")
    private int readTimeout;
    @Value("${bfh.skipExit:false}")
    private boolean skipExit;

    private final WebhookService webhookService;

    public App(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @Bean
    public org.springframework.web.client.RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return new org.springframework.web.client.RestTemplate(factory);
    }

    @Override
    public void run(String... args) {
        System.out.println("Starting BFH Java Qualifier App...");
        GenerateResponse response = webhookService.generateWebhook(name, regNo, email);
        if (response == null || response.getWebhook() == null || response.getAccessToken() == null) {
            System.err.println("Invalid webhook response. Exiting.");
            if (!skipExit) System.exit(1);
            return;
        }
        String lastTwo = regNo.replaceAll("[^0-9]", "");
        lastTwo = lastTwo.length() >= 2 ? lastTwo.substring(lastTwo.length() - 2) : lastTwo;
        int dd = Integer.parseInt(lastTwo);
        String finalQuery = (dd % 2 == 1) ? SQLProvider.SQL_Q1 : SQLProvider.SQL_Q2;
        // Save to src/main/resources/finalQuery.sql
        try (FileWriter fw = new FileWriter("src/main/resources/finalQuery.sql")) {
            fw.write(finalQuery);
        } catch (IOException e) {
            System.err.println("Failed to write finalQuery.sql to resources: " + e.getMessage());
        }
        // Save to root finalQuery.sql
        try (FileWriter fw = new FileWriter("finalQuery.sql")) {
            fw.write(finalQuery);
        } catch (IOException e) {
            System.err.println("Failed to write finalQuery.sql to root: " + e.getMessage());
        }
        String submitResponse = webhookService.submitFinalQuery(response.getWebhook(), response.getAccessToken(), finalQuery);
        System.out.println("Submission response: " + submitResponse);
        if (!skipExit) System.exit(0);
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
