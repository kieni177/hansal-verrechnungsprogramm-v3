package com.hansal.verrechnungsprogramm.controller;

import com.hansal.verrechnungsprogramm.model.Invoice;
import com.hansal.verrechnungsprogramm.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        log.debug("GET /api/invoices");
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        log.debug("GET /api/invoices/{}", id);
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping("/number/{invoiceNumber}")
    public ResponseEntity<Invoice> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        log.debug("GET /api/invoices/number/{}", invoiceNumber);
        return ResponseEntity.ok(invoiceService.getInvoiceByNumber(invoiceNumber));
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<Invoice> getInvoiceByOrderId(@PathVariable Long orderId) {
        log.debug("GET /api/invoices/by-order/{}", orderId);
        return invoiceService.getInvoiceByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/from-order/{orderId}")
    public ResponseEntity<Invoice> createInvoiceFromOrder(@PathVariable Long orderId) {
        log.debug("POST /api/invoices/from-order/{}", orderId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invoiceService.createInvoiceFromOrder(orderId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Invoice> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody Invoice invoice) {
        log.debug("PUT /api/invoices/{}", id);
        return ResponseEntity.ok(invoiceService.updateInvoice(id, invoice));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        log.debug("DELETE /api/invoices/{}", id);
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        log.debug("GET /api/invoices/{}/pdf", id);
        Invoice invoice = invoiceService.getInvoiceById(id);
        byte[] pdfBytes = invoiceService.generateInvoicePdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String customerName = invoice.getOrder().getCustomerName()
                .replaceAll("[^a-zA-ZäöüÄÖÜß\\s]", "")
                .replaceAll("\\s+", "_");
        headers.setContentDispositionFormData("attachment",
                "beleg_" + customerName + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PostMapping("/batch/pdf")
    public ResponseEntity<byte[]> downloadCombinedPdf(@RequestBody List<Long> invoiceIds) {
        log.debug("POST /api/invoices/batch/pdf - {} invoices", invoiceIds != null ? invoiceIds.size() : 0);
        if (invoiceIds == null || invoiceIds.isEmpty()) {
            log.warn("Batch PDF request with empty invoice list");
            return ResponseEntity.badRequest().build();
        }

        byte[] pdfBytes = invoiceService.generateCombinedPdf(invoiceIds);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String timestamp = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        headers.setContentDispositionFormData("attachment",
                "belege_sammel_" + timestamp + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
