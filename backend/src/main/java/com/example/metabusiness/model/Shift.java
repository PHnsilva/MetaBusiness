package com.example.metabusiness.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "shifts")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long attendantId;
    private Instant startTime;
    private Instant endTime;

    // getters e setters
    public Long getId() { return id; }

    public Long getAttendantId() { return attendantId; }
    public void setAttendantId(Long attendantId) {
        this.attendantId = attendantId;
    }

    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
}
