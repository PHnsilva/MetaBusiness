package com.example.metabusiness.service;

import com.example.metabusiness.model.*;
import com.example.metabusiness.repository.ConversationRepository;
import com.example.metabusiness.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final WhatsAppService whatsAppService;
    private final ShiftService shiftService;

    public ConversationService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            WhatsAppService whatsAppService,
            ShiftService shiftService
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.whatsAppService = whatsAppService;
        this.shiftService = shiftService;
    }

    public void processIncomingMessage(String phone, String text) {

        Conversation conversation = conversationRepository
                .findByPhone(phone)
                .orElseGet(() -> createConversation(phone));

        saveMessage(conversation.getId(), phone, text, true);

        conversation.setLastCustomerMessage(Instant.now());
        conversation.setLastUpdated(Instant.now());

        if (conversation.getStatus() == ConversationStatus.HUMAN) {
            conversationRepository.save(conversation);
            return;
        }

        // Simples detecção de pedido de humano
        if (text.toLowerCase().contains("atendente")
                && shiftService.hasAvailableAttendantNow()) {

            conversation.setStatus(ConversationStatus.HUMAN);
            conversationRepository.save(conversation);
            return;
        }

        // Bot responde (1 ou 2 mensagens simples)
        if (within24h(conversation.getLastCustomerMessage())) {
            whatsAppService.sendTextMessage(phone,
                    "Posso te ajudar com algo específico ou deseja falar com um atendente?");
        } else {
            whatsAppService.sendTemplate(
                    phone,
                    "fora_horario",
                    null
            );
        }

        conversationRepository.save(conversation);
    }

    private Conversation createConversation(String phone) {
        Conversation c = new Conversation();
        c.setPhone(phone);
        c.setStatus(ConversationStatus.BOT);
        c.setLastUpdated(Instant.now());
        return conversationRepository.save(c);
    }

    private void saveMessage(Long conversationId, String from, String text, boolean fromCustomer) {
        Message m = new Message();
        m.setConversationId(conversationId);
        m.setFromNumber(from);
        m.setFromCustomer(fromCustomer);
        m.setText(text);
        m.setTimestamp(Instant.now());
        messageRepository.save(m);
    }

    private boolean within24h(Instant lastCustomerMessage) {
        if (lastCustomerMessage == null) return false;
        return lastCustomerMessage.isAfter(
                Instant.now().minus(24, ChronoUnit.HOURS)
        );
    }
}
