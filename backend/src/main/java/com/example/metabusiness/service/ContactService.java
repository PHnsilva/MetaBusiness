package com.example.metabusiness.service;

import com.example.metabusiness.model.Contact;
import com.example.metabusiness.repository.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final ContactRepository repository;
    private final WhatsAppService whatsappService;

    public ContactService(ContactRepository repository,
                          WhatsAppService whatsappService) {
        this.repository = repository;
        this.whatsappService = whatsappService;
    }

    public Contact save(Contact contact) {

        Contact saved = repository.save(contact);

        // Dispara WhatsApp após salvar
        whatsappService.sendWelcomeMessage(
                contact.getPhone(),
                contact.getName()
        );

        return saved;
    }

    public List<Contact> findAll() {
        return repository.findAll();
    }
}
