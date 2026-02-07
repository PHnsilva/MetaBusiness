package com.example.metabusiness.controller;

import com.example.metabusiness.service.WhatsAppService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/webhook/whatsapp")
public class WebhookController {

    private final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @Value("${meta.webhook.verify-token:MEU_TOKEN_TESTE}")
    private String verifyToken;

    /**
     * Opcional: se definido, será usado para validar o header X-Hub-Signature-256.
     * Se estiver vazio, a verificação será ignorada (mas logada).
     */
    @Value("${meta.app.secret:}")
    private String appSecret;

    private final WhatsAppService whatsAppService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    // Recebendo o corpo bruto (String) para permitir verificação de assinatura HMAC
    @PostMapping
    public ResponseEntity<String> receiveWebhook(
            @RequestBody String rawBody,
            @RequestHeader Map<String, String> headers
    ) {
        log.debug("POST /webhook/whatsapp rawBody: {}", rawBody);

        // Verificação opcional de assinatura (recomendada em produção)
        try {
            String signatureHeader = headers.entrySet().stream()
                    .filter(e -> e.getKey() != null && e.getKey().equalsIgnoreCase("X-Hub-Signature-256"))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);

            if (appSecret != null && !appSecret.isBlank()) {
                if (signatureHeader == null) {
                    log.warn("App secret configurado mas assinatura não encontrada no header.");
                } else if (!verifyHmacSha256(rawBody, signatureHeader, appSecret)) {
                    log.warn("Assinatura inválida. Ignorando evento.");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Assinatura inválida");
                }
            } else {
                log.debug("meta.app.secret não configurado — pulando verificação de assinatura.");
            }
        } catch (Exception ex) {
            log.error("Erro ao verificar assinatura: {}", ex.getMessage(), ex);
            // Não interrompe a execução — mas em produção você pode optar por retornar 403.
        }

        // Parse do JSON
        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(rawBody, Map.class);
        } catch (Exception ex) {
            log.error("Erro ao desserializar payload do webhook: {}", ex.getMessage(), ex);
            return ResponseEntity.badRequest().body("Payload inválido");
        }

        log.info("Webhook payload recebido (resumido): keys={}", payload.keySet());

        try {
            Optional<MessageInfo> maybe = extractMessageInfo(payload);
            if (maybe.isPresent()) {
                MessageInfo info = maybe.get();
                log.info("Mensagem recebida de {}: {} (messageId={})", info.from, info.text, info.messageId);

                // Aqui você pode encadear com ConversationService (se existir)
                // Por enquanto, responde automaticamente com texto simples
                if (info.from != null) {
                    whatsAppService.sendTextMessage(info.from, "Recebemos sua mensagem — obrigado!");
                }
            } else {
                log.debug("Nenhuma mensagem legível encontrada no payload (pode ser status/event).");
            }
        } catch (Exception ex) {
            log.error("Erro ao processar payload do webhook: {}", ex.getMessage(), ex);
        }

        // Responder rápido para o Meta
        return ResponseEntity.ok("EVENT_RECEIVED");
    }

    /**
     * Tenta extrair telefone (wa_id) e conteúdo do payload do WhatsApp.
     * Lida com payloads típicos da API (entry -> changes -> value -> messages).
     */
    private Optional<MessageInfo> extractMessageInfo(Map<String, Object> payload) {
        try {
            var entryObj = payload.get("entry");
            if (!(entryObj instanceof List)) return Optional.empty();
            var entryList = (List<?>) entryObj;
            if (entryList.isEmpty()) return Optional.empty();

            var entry0 = asMap(entryList.get(0));
            if (entry0 == null) return Optional.empty();

            var changesObj = entry0.get("changes");
            if (!(changesObj instanceof List)) return Optional.empty();
            var changes = (List<?>) changesObj;
            if (changes.isEmpty()) return Optional.empty();

            var change0 = asMap(changes.get(0));
            if (change0 == null) return Optional.empty();

            var value = asMap(change0.get("value"));
            if (value == null) return Optional.empty();

            // Mensagens normais
            var messagesObj = value.get("messages");
            if (messagesObj instanceof List && !((List<?>) messagesObj).isEmpty()) {
                var message0 = asMap(((List<?>) messagesObj).get(0));
                if (message0 == null) return Optional.empty();
                String from = (String) message0.get("from");
                String messageId = (String) message0.get("id");
                String text = null;

                // text
                var textObj = asMap(message0.get("text"));
                if (textObj != null) {
                    text = (String) textObj.get("body");
                } else if (message0.get("button") instanceof Map) {
                    // botão pressionado (reply/button)
                    var button = asMap(message0.get("button"));
                    if (button != null) text = (String) button.get("text");
                } else if (message0.get("interactive") instanceof Map) {
                    var interactive = asMap(message0.get("interactive"));
                    // pode ser button_reply ou list_reply
                    var type = (String) interactive.get("type");
                    if ("button_reply".equals(type) && interactive.get("button_reply") instanceof Map) {
                        var br = asMap(interactive.get("button_reply"));
                        text = (String) br.get("title");
                    } else if ("list_reply".equals(type) && interactive.get("list_reply") instanceof Map) {
                        var lr = asMap(interactive.get("list_reply"));
                        text = (String) lr.get("title");
                    }
                }

                return Optional.of(new MessageInfo(from, text, messageId));
            }

            // Outros eventos (statuses, messaging_product, etc) -> ignorar por enquanto
            return Optional.empty();

        } catch (Exception ex) {
            log.error("Erro ao extrair mensagem do payload: {}", ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private Map<String, Object> asMap(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        return null;
    }

    private static class MessageInfo {
        final String from;
        final String text;
        final String messageId;
        MessageInfo(String from, String text, String messageId) {
            this.from = from;
            this.text = text;
            this.messageId = messageId;
        }
    }

    private boolean verifyHmacSha256(String payload, String signatureHeader, String secret) {
        try {
            if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
                log.warn("Formato de signature header inesperado: {}", signatureHeader);
                return false;
            }
            String sig = signatureHeader.substring("sha256=".length());
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = bytesToHex(digest);
            boolean ok = MessageDigestIsEqual(expected, sig);
            if (!ok) {
                log.warn("Assinatura HMAC não confere. esperado={} recebido={}", expected, sig);
            }
            return ok;
        } catch (Exception ex) {
            log.error("Erro ao calcular HMAC-SHA256: {}", ex.getMessage(), ex);
            return false;
        }
    }

    // constante-safe equals
    private boolean MessageDigestIsEqual(String aHex, String bHex) {
        try {
            byte[] a = hexStringToByteArray(aHex);
            byte[] b = hexStringToByteArray(bHex);
            if (a == null || b == null) return false;
            if (a.length != b.length) return false;
            int result = 0;
            for (int i = 0; i < a.length; i++) result |= a[i] ^ b[i];
            return result == 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte[] hexStringToByteArray(String s) {
        try {
            int len = s.length();
            if (len % 2 != 0) return null;
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i+1), 16));
            }
            return data;
        } catch (Exception ex) {
            return null;
        }
    }
}
