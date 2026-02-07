package com.example.metabusiness.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConversationStatus status;

    private Instant lastCustomerMessage;
    private Instant lastUpdated;

    private Long attendantId; // null se ninguém assumiu

    // getters e setters
    public Long getId() { return id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public ConversationStatus getStatus() { return status; }
    public void setStatus(ConversationStatus status) { this.status = status; }

    public Instant getLastCustomerMessage() { return lastCustomerMessage; }
    public void setLastCustomerMessage(Instant lastCustomerMessage) {
        this.lastCustomerMessage = lastCustomerMessage;
    }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getAttendantId() { return attendantId; }
    public void setAttendantId(Long attendantId) {
        this.attendantId = attendantId;
    }
}
