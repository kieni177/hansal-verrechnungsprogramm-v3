package com.hansal.verrechnungsprogramm.repository;

import com.hansal.verrechnungsprogramm.model.Invoice;
import com.hansal.verrechnungsprogramm.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByStatus(InvoiceStatus status);
    Optional<Invoice> findByOrderId(Long orderId);

    @Query("SELECT COALESCE(MAX(i.id), 0) FROM Invoice i")
    Long findMaxId();
}
