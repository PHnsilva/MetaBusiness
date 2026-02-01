package com.example.metabusiness.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WhatsAppService {

    @Value("${meta.whatsapp.token}")
    private String token;

    @Value("${meta.whatsapp.phone-id}")
    private String phoneId;

    @Value("${meta.whatsapp.version}")
    private String version;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendWelcomeMessage(String phone, String name) {

        String url = "https://graph.facebook.com/" + version + "/" + phoneId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = Map.of(
            "messaging_product", "whatsapp",
            "to", phone,
            "type", "text",
            "text", Map.of(
                "body", "Olá " + name + "! 👋\nSeu contato foi cadastrado com sucesso."
            )
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        restTemplate.postForEntity(url, request, String.class);
    }
}
