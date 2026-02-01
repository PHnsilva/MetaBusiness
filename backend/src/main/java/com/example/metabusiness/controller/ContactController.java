package com.example.metabusiness.controller;

import com.example.metabusiness.model.Contact;
import com.example.metabusiness.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
@CrossOrigin(origins = "http://localhost:5173")
public class ContactController {

    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Contact> create(@Valid @RequestBody Contact contact) {
        return ResponseEntity.ok(service.save(contact));
    }

    @GetMapping
    public ResponseEntity<List<Contact>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }
}
