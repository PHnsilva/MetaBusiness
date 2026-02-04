package com.example.metabusiness.controller;

import com.example.metabusiness.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook/whatsapp")
public class WebhookController {

    private final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @Value("${meta.webhook.verify-token:MEU_TOKEN_TESTE}")
    private String verifyToken;

    private final WhatsAppService whatsAppService;

    public WebhookController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    // Verificação que o Meta usa (GET)
    @GetMapping
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.challenge", required = false) String challenge,
            @RequestParam(name = "hub.verify_token", required = false) String token
    ) {
        log.info("GET /webhook/whatsapp called - mode={}, token={}", mode, token);
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge != null ? challenge : "");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token inválido");
    }

    // Recebe eventos do Meta (POST) — aqui você processa mensagens
    @PostMapping
    public ResponseEntity<String> receiveWebhook(@RequestBody Map<String, Object> payload) {
        log.info("POST /webhook/whatsapp payload: {}", payload);

        // Exemplo básico: tentar extrair telefone e enviar auto-reply (ajuste conforme seu payload)
        try {
            // A estrutura do payload do WhatsApp é aninhada; isso é só um exemplo simples.
            // Recomendo adaptar o parsing conforme o JSON real recebido.
            var entry = ((java.util.List<?>) payload.get("entry"));
            if (entry != null && !entry.isEmpty()) {
                var entry0 = (Map<?, ?>) entry.get(0);
                var changes = (java.util.List<?>) entry0.get("changes");
                if (changes != null && !changes.isEmpty()) {
                    var change0 = (Map<?, ?>) changes.get(0);
                    var value = (Map<?, ?>) change0.get("value");
                    var messages = (java.util.List<?>) value.get("messages");
                    if (messages != null && !messages.isEmpty()) {
                        var message0 = (Map<?, ?>) messages.get(0);
                        String from = (String) message0.get("from"); // wa_id
                        String text = null;
                        var textObj = (Map<?, ?>) message0.get("text");
                        if (textObj != null) {
                            text = (String) textObj.get("body");
                        }

                        log.info("Mensagem de {}: {}", from, text);

                        // Exemplo: responder com texto simples (não template)
                        if (from != null) {
                            whatsAppService.sendTextMessage(from, "Recebemos sua mensagem — obrigado!");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Erro ao processar payload do webhook: {}", ex.getMessage(), ex);
        }

        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}
