package com.seuprojeto.repository;

import com.seuprojeto.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface ShiftRepository extends JpaRepository<Shift, Long> {

    @Query("""
        SELECT s FROM Shift s
        WHERE s.startTime <= :now
          AND s.endTime >= :now
    """)
    List<Shift> findActiveAt(Instant now);
}
