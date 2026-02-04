package com.example.metabusiness.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class WhatsAppService {

    private final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${meta.whatsapp.token:}")
    private String token;

    @Value("${meta.whatsapp.phone-id:}")
    private String phoneId;

    @Value("${meta.whatsapp.version:v19.0}")
    private String version;

    private final RestTemplate restTemplate = new RestTemplate();

    private boolean configured() {
        return token != null && !token.isBlank() && phoneId != null && !phoneId.isBlank();
    }

    /**
     * Envia uma mensagem de texto simples (resposta imediata).
     */
    public void sendTextMessage(String phone, String bodyText) {
        if (!configured()) {
            log.warn("[WhatsAppService] Não configurado (token/phoneId ausentes). Ignorando envio para {}", phone);
            return;
        }

        String url = "https://graph.facebook.com/" + version + "/" + phoneId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> payload = Map.of(
            "messaging_product", "whatsapp",
            "to", phone,
            "type", "text",
            "text", Map.of("body", bodyText)
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(url, request, String.class);
            log.info("[WhatsAppService] sendTextMessage status={} body={}", resp.getStatusCodeValue(), resp.getBody());
        } catch (Exception e) {
            log.error("[WhatsAppService] Erro ao enviar text message para {}: {}", phone, e.getMessage());
        }
    }

    /**
     * Envia uma mensagem de boas-vindas usando template ou texto conforme configuração.
     * Aqui usamos template se você quiser — você pode alterar para template quando estiver pronto.
     */
    public void sendWelcomeMessage(String phone, String name) {
        if (!configured()) {
            log.warn("[WhatsAppService] sendWelcomeMessage SKIPPED (not configured) for {}", phone);
            return;
        }

        // exemplo: enviar template se você tiver template com {{1}}.
        // Se preferir enviar texto simples, descomente a linha sendTextMessage abaixo.
        try {
            // Exemplo usando TEXT:
            String text = "Olá " + (name != null ? name : "") + "! Seu contato foi cadastrado com sucesso.";
            sendTextMessage(phone, text);

            // Se quiser usar template (apenas se seu template aceitar 1 param),
            // substitua por uma implementação de template como no exemplo anterior.
        } catch (Exception ex) {
            log.error("[WhatsAppService] Erro em sendWelcomeMessage para {}: {}", phone, ex.getMessage());
        }
    }
}
