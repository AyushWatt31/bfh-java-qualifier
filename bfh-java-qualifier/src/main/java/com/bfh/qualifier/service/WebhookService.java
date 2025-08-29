package com.bfh.qualifier.service;

import com.bfh.qualifier.dto.GenerateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {
    private final RestTemplate restTemplate;
    private final String fallbackUrl;

    public WebhookService(RestTemplate restTemplate,
                          @Value("${bfh.fallbackUrl}") String fallbackUrl) {
        this.restTemplate = restTemplate;
        this.fallbackUrl = fallbackUrl;
    }

    public GenerateResponse generateWebhook(String name, String regNo, String email) {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        Map<String, String> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("regNo", regNo);
        payload.put("email", email);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<GenerateResponse> response = restTemplate.postForEntity(url, entity, GenerateResponse.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("Error generating webhook: " + e.getMessage());
            return null;
        }
    }

    public String submitFinalQuery(String webhookUrl, String token, String finalQuery) {
        Map<String, String> payload = new HashMap<>();
        payload.put("finalQuery", finalQuery);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, entity, String.class);
            return response.getBody();
        } catch (RestClientException e) {
            System.err.println("Webhook submission failed, trying fallback: " + e.getMessage());
            try {
                ResponseEntity<String> fallbackResponse = restTemplate.postForEntity(fallbackUrl, entity, String.class);
                return fallbackResponse.getBody();
            } catch (RestClientException ex) {
                System.err.println("Fallback submission failed: " + ex.getMessage());
                return null;
            }
        }
    }
}
