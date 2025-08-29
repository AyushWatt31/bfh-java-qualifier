package com.bfh.qualifier.service;

import com.bfh.qualifier.dto.GenerateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(WebhookService.class)
public class WebhookServiceTest {
    @Autowired
    private WebhookService webhookService;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void testGenerateWebhook() {
        String responseJson = "{\"webhook\":\"https://webhook.url\",\"accessToken\":\"jwt-token\"}";
        mockServer.expect(requestTo("https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        GenerateResponse response = webhookService.generateWebhook("Test Name", "REG12345", "test@example.com");
        assertThat(response).isNotNull();
        assertThat(response.getWebhook()).isEqualTo("https://webhook.url");
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
    }

    @Test
    void testSubmitFinalQuerySuccess() {
        String webhookUrl = "https://webhook.url";
        String token = "jwt-token";
        String finalQuery = "SELECT 1;";
        mockServer.expect(requestTo(webhookUrl))
                .andExpect(header("Authorization", token))
                .andRespond(withSuccess("OK", MediaType.TEXT_PLAIN));

        String response = webhookService.submitFinalQuery(webhookUrl, token, finalQuery);
        assertThat(response).isEqualTo("OK");
    }

    @Test
    void testSubmitFinalQueryFallback() {
        String webhookUrl = "https://webhook.url";
        String token = "jwt-token";
        String finalQuery = "SELECT 1;";
        String fallbackUrl = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";
        mockServer.expect(requestTo(webhookUrl))
                .andRespond(withServerError());
        mockServer.expect(requestTo(fallbackUrl))
                .andRespond(withSuccess("FALLBACK_OK", MediaType.TEXT_PLAIN));

        String response = webhookService.submitFinalQuery(webhookUrl, token, finalQuery);
        assertThat(response).isEqualTo("FALLBACK_OK");
    }
}
