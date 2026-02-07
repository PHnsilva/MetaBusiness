package com.example.metabusiness.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long conversationId;
    private String fromNumber;
    private boolean fromCustomer;

    @Column(length = 4000)
    private String text;

    private Instant timestamp;

    // getters e setters
    public Long getId() { return id; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getFromNumber() { return fromNumber; }
    public void setFromNumber(String fromNumber) {
        this.fromNumber = fromNumber;
    }

    public boolean isFromCustomer() { return fromCustomer; }
    public void setFromCustomer(boolean fromCustomer) {
        this.fromCustomer = fromCustomer;
    }

    public String getText() { return text; }
    public void setText(String text) {
        this.text = text;
    }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
