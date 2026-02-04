package com.example.metabusiness.service;

import com.example.metabusiness.model.Contact;
import com.example.metabusiness.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository repository;
    private final WhatsAppService whatsappService; // note o nome consistente 'whatsappService'

    public ContactService(ContactRepository repository,
                          WhatsAppService whatsappService) {
        this.repository = repository;
        this.whatsappService = whatsappService;
    }

    public Contact save(Contact contact) {
        // normalize phone if you want
        String normalizedPhone = contact.getPhone() != null ? contact.getPhone().replaceAll("[^0-9]", "") : null;
        contact.setPhone(normalizedPhone);

        Contact saved = repository.save(contact);
        log.info("Contato salvo: {} - {}", saved.getId(), saved.getPhone());

        // Dispara WhatsApp após salvar (envio protegido dentro do service)
        try {
            // envia mensagem de boas-vindas (pode usar template ou texto)
            whatsappService.sendWelcomeMessage(saved.getPhone(), saved.getName());
        } catch (Exception ex) {
            // não deixar a falha no envio quebrar o fluxo de salvar o contato
            log.error("Erro ao disparar WhatsApp para {}: {}", saved.getPhone(), ex.getMessage());
        }

        return saved;
    }

    public List<Contact> findAll() {
        return repository.findAll();
    }
}
