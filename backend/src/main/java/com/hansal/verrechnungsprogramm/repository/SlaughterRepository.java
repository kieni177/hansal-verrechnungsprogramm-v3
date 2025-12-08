package com.hansal.verrechnungsprogramm.repository;

import com.hansal.verrechnungsprogramm.model.Slaughter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SlaughterRepository extends JpaRepository<Slaughter, Long> {
    List<Slaughter> findByCowTagContainingIgnoreCase(String cowTag);
    List<Slaughter> findBySlaughterDateBetween(LocalDate startDate, LocalDate endDate);
    List<Slaughter> findByCowId(String cowId);
}
