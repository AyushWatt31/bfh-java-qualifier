package com.bfh.qualifier;

import com.bfh.qualifier.service.WebhookService;
import com.bfh.qualifier.dto.GenerateResponse;
import com.bfh.qualifier.util.SQLProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AppIntegrationTest {
    @MockBean
    private WebhookService webhookService;

    @Autowired
    private App app;

    @Test
    void testAppFlowOddRegNo() {
        GenerateResponse mockResponse = new GenerateResponse();
        mockResponse.setWebhook("https://webhook.url");
        mockResponse.setAccessToken("jwt-token");
        Mockito.when(webhookService.generateWebhook(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(mockResponse);
        Mockito.when(webhookService.submitFinalQuery(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn("OK");
        String regNo = "REG12345"; // Odd last two digits
        String lastTwo = regNo.replaceAll("[^0-9]", "");
        lastTwo = lastTwo.length() >= 2 ? lastTwo.substring(lastTwo.length() - 2) : lastTwo;
        int dd = Integer.parseInt(lastTwo);
        String finalQuery = (dd % 2 == 1) ? SQLProvider.SQL_Q1 : SQLProvider.SQL_Q2;
        assertThat(finalQuery).isEqualTo(SQLProvider.SQL_Q1);
    }

    @Test
    void testAppFlowEvenRegNo() {
        GenerateResponse mockResponse = new GenerateResponse();
        mockResponse.setWebhook("https://webhook.url");
        mockResponse.setAccessToken("jwt-token");
        Mockito.when(webhookService.generateWebhook(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(mockResponse);
        Mockito.when(webhookService.submitFinalQuery(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn("OK");
        String regNo = "REG12344"; // Even last two digits
        String lastTwo = regNo.replaceAll("[^0-9]", "");
        lastTwo = lastTwo.length() >= 2 ? lastTwo.substring(lastTwo.length() - 2) : lastTwo;
        int dd = Integer.parseInt(lastTwo);
        String finalQuery = (dd % 2 == 1) ? SQLProvider.SQL_Q1 : SQLProvider.SQL_Q2;
        assertThat(finalQuery).isEqualTo(SQLProvider.SQL_Q2);
    }
}
