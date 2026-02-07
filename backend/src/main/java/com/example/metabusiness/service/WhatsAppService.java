package com.example.metabusiness.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.*;

/**
 * Serviço mínimo para envio de mensagens via Graph API do WhatsApp (Meta).
 * - Configure meta.whatsapp.token e meta.whatsapp.phone-id no application.properties/env.
 * - Opcional: meta.whatsapp.version (por padrão v19.0).
 */
@Service
public class WhatsAppService {

    private final Logger log = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${meta.whatsapp.token:}")
    private String token;

    @Value("${meta.whatsapp.phone-id:}")
    private String phoneId;

    @Value("${meta.whatsapp.version:v19.0}")
    private String version;

    // Timeout configurável via SimpleClientHttpRequestFactory (evita hangs)
    private final RestTemplate restTemplate;

    public WhatsAppService() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(5000); // ms
        f.setReadTimeout(10000); // ms
        this.restTemplate = new RestTemplate(f);
    }

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
            log.info("[WhatsAppService] sendTextMessage -> status={} body={}", resp.getStatusCodeValue(), resp.getBody());
        } catch (Exception e) {
            log.error("[WhatsAppService] Erro ao enviar text message para {}: {}", phone, e.getMessage(), e);
        }
    }

    /**
     * Envia um template. params é um Map com chaves por ordem (param1,param2...) ou qualquer ordem;
     * os valores serão colocados como parâmetros de body (type=text).
     *
     * Exemplo de uso:
     * sendTemplate("551199999", "meu_template", Map.of("1", "João", "2", "12/02"));
     */
    public void sendTemplate(String phone, String templateName, Map<String, String> params) {
        if (!configured()) {
            log.warn("[WhatsAppService] sendTemplate SKIPPED (not configured) for {}", phone);
            return;
        }

        String url = "https://graph.facebook.com/" + version + "/" + phoneId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        // Monta lista de parâmetros compatível com template body
        List<Map<String, Object>> parameters = new ArrayList<>();
        if (params != null && !params.isEmpty()) {
            // ordena por chave numerica se for "1","2",...
            List<String> keys = new ArrayList<>(params.keySet());
            keys.sort(Comparator.comparingInt(k -> {
                try { return Integer.parseInt(k.replaceAll("\\D", "")); } catch (Exception ex) { return Integer.MAX_VALUE; }
            }));
            for (String k : keys) {
                String v = params.get(k);
                if (v == null) continue;
                parameters.add(Map.of("type", "text", "text", v));
            }
        }

        Map<String, Object> templateMap = new HashMap<>();
        templateMap.put("name", templateName);
        templateMap.put("language", Map.of("code", "pt_BR"));

        if (!parameters.isEmpty()) {
            Map<String, Object> component = Map.of(
                    "type", "body",
                    "parameters", parameters
            );
            templateMap.put("components", List.of(component));
        }

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", phone,
                "type", "template",
                "template", templateMap
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(url, request, String.class);
            log.info("[WhatsAppService] sendTemplate -> status={} body={}", resp.getStatusCodeValue(), resp.getBody());
        } catch (Exception e) {
            log.error("[WhatsAppService] Erro ao enviar template '{}' para {}: {}", templateName, phone, e.getMessage(), e);
        }
    }
}
