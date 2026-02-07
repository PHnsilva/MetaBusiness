package com.example.metabusiness.service;

import com.example.metabusiness.model.Shift;
import com.example.metabusiness.repository.ShiftRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ShiftService {

    private final ShiftRepository shiftRepository;

    public ShiftService(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public boolean hasAvailableAttendantNow() {
        List<Shift> shifts = shiftRepository.findActiveAt(Instant.now());
        return !shifts.isEmpty(); // pode refinar com status online
    }
}
