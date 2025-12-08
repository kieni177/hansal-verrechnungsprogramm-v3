package com.hansal.verrechnungsprogramm.repository;

import com.hansal.verrechnungsprogramm.model.MeatCut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MeatCutRepository extends JpaRepository<MeatCut, Long> {
    List<MeatCut> findBySlaughterId(Long slaughterId);

    @Query("SELECT m FROM MeatCut m WHERE m.availableWeight > 0 ORDER BY m.slaughter.slaughterDate DESC")
    List<MeatCut> findAllAvailable();

    @Query("SELECT m FROM MeatCut m WHERE m.product.id = :productId AND m.availableWeight >= :minWeight")
    List<MeatCut> findByProductIdAndMinWeight(Long productId, BigDecimal minWeight);

    List<MeatCut> findByProductId(Long productId);

    @Query("SELECT m FROM MeatCut m WHERE m.product.id = :productId AND m.availableWeight > 0 ORDER BY m.slaughter.slaughterDate DESC")
    List<MeatCut> findAvailableByProductId(Long productId);
}
