package com.hansal.verrechnungsprogramm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hansal.verrechnungsprogramm.model.Invoice;
import com.hansal.verrechnungsprogramm.model.InvoiceStatus;
import com.hansal.verrechnungsprogramm.model.Order;
import com.hansal.verrechnungsprogramm.model.OrderStatus;
import com.hansal.verrechnungsprogramm.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InvoiceController.class)
@WithMockUser
class InvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InvoiceService invoiceService;

    private Invoice testInvoice;
    private Order testOrder;
    private List<Invoice> testInvoices;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomerName("Max Mustermann");
        testOrder.setCustomerPhone("+43 660 1234567");
        testOrder.setStatus(OrderStatus.COMPLETED);
        testOrder.setTotalAmount(new BigDecimal("150.00"));
        testOrder.setOrderDate(LocalDateTime.now());

        testInvoice = new Invoice();
        testInvoice.setId(1L);
        testInvoice.setInvoiceNumber("INV-2024-000001");
        testInvoice.setOrder(testOrder);
        testInvoice.setIssueDate(LocalDate.now());
        testInvoice.setDueDate(LocalDate.now().plusDays(30));
        testInvoice.setTotalAmount(new BigDecimal("150.00"));
        testInvoice.setTaxRate(BigDecimal.ZERO);
        testInvoice.setTaxAmount(BigDecimal.ZERO);
        testInvoice.setGrandTotal(new BigDecimal("150.00"));
        testInvoice.setStatus(InvoiceStatus.UNPAID);
        testInvoice.setCreatedAt(LocalDateTime.now());
        testInvoice.setUpdatedAt(LocalDateTime.now());

        Invoice invoice2 = new Invoice();
        invoice2.setId(2L);
        invoice2.setInvoiceNumber("INV-2024-000002");
        invoice2.setOrder(testOrder);
        invoice2.setIssueDate(LocalDate.now());
        invoice2.setTotalAmount(new BigDecimal("200.00"));
        invoice2.setGrandTotal(new BigDecimal("200.00"));
        invoice2.setStatus(InvoiceStatus.PAID);

        testInvoices = Arrays.asList(testInvoice, invoice2);
    }

    @Test
    @DisplayName("GET /api/invoices - Should return all invoices")
    void getAllInvoices_ShouldReturnAllInvoices() throws Exception {
        when(invoiceService.getAllInvoices()).thenReturn(testInvoices);

        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].invoiceNumber", is("INV-2024-000001")))
                .andExpect(jsonPath("$[1].invoiceNumber", is("INV-2024-000002")));

        verify(invoiceService, times(1)).getAllInvoices();
    }

    @Test
    @DisplayName("GET /api/invoices/{id} - Should return invoice by ID")
    void getInvoiceById_ShouldReturnInvoice() throws Exception {
        when(invoiceService.getInvoiceById(1L)).thenReturn(testInvoice);

        mockMvc.perform(get("/api/invoices/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.invoiceNumber", is("INV-2024-000001")))
                .andExpect(jsonPath("$.status", is("UNPAID")));

        verify(invoiceService, times(1)).getInvoiceById(1L);
    }

    @Test
    @DisplayName("GET /api/invoices/number/{invoiceNumber} - Should return invoice by number")
    void getInvoiceByNumber_ShouldReturnInvoice() throws Exception {
        when(invoiceService.getInvoiceByNumber("INV-2024-000001")).thenReturn(testInvoice);

        mockMvc.perform(get("/api/invoices/number/INV-2024-000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceNumber", is("INV-2024-000001")));

        verify(invoiceService, times(1)).getInvoiceByNumber("INV-2024-000001");
    }

    @Test
    @DisplayName("GET /api/invoices/by-order/{orderId} - Should return invoice by order ID")
    void getInvoiceByOrderId_WhenExists_ShouldReturnInvoice() throws Exception {
        when(invoiceService.getInvoiceByOrderId(1L)).thenReturn(Optional.of(testInvoice));

        mockMvc.perform(get("/api/invoices/by-order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id", is(1)));

        verify(invoiceService, times(1)).getInvoiceByOrderId(1L);
    }

    @Test
    @DisplayName("GET /api/invoices/by-order/{orderId} - Should return 404 when not found")
    void getInvoiceByOrderId_WhenNotExists_ShouldReturnNotFound() throws Exception {
        when(invoiceService.getInvoiceByOrderId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/invoices/by-order/999"))
                .andExpect(status().isNotFound());

        verify(invoiceService, times(1)).getInvoiceByOrderId(999L);
    }

    @Test
    @DisplayName("POST /api/invoices/from-order/{orderId} - Should create invoice from order")
    void createInvoiceFromOrder_ShouldReturnCreatedInvoice() throws Exception {
        when(invoiceService.createInvoiceFromOrder(1L)).thenReturn(testInvoice);

        mockMvc.perform(post("/api/invoices/from-order/1")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.invoiceNumber", is("INV-2024-000001")));

        verify(invoiceService, times(1)).createInvoiceFromOrder(1L);
    }

    @Test
    @DisplayName("PUT /api/invoices/{id} - Should update invoice")
    void updateInvoice_ShouldReturnUpdatedInvoice() throws Exception {
        Invoice updatedInvoice = new Invoice();
        updatedInvoice.setId(1L);
        updatedInvoice.setInvoiceNumber("INV-2024-000001");
        updatedInvoice.setOrder(testOrder);
        updatedInvoice.setStatus(InvoiceStatus.PAID);
        updatedInvoice.setNotes("Payment received");

        when(invoiceService.updateInvoice(eq(1L), any(Invoice.class))).thenReturn(updatedInvoice);

        mockMvc.perform(put("/api/invoices/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedInvoice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PAID")))
                .andExpect(jsonPath("$.notes", is("Payment received")));

        verify(invoiceService, times(1)).updateInvoice(eq(1L), any(Invoice.class));
    }

    @Test
    @DisplayName("DELETE /api/invoices/{id} - Should delete invoice")
    void deleteInvoice_ShouldReturnNoContent() throws Exception {
        doNothing().when(invoiceService).deleteInvoice(1L);

        mockMvc.perform(delete("/api/invoices/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(invoiceService, times(1)).deleteInvoice(1L);
    }

    @Test
    @DisplayName("GET /api/invoices/{id}/pdf - Should download invoice PDF")
    void downloadInvoicePdf_ShouldReturnPdf() throws Exception {
        byte[] pdfBytes = "PDF content".getBytes();
        when(invoiceService.getInvoiceById(1L)).thenReturn(testInvoice);
        when(invoiceService.generateInvoicePdf(1L)).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/invoices/1/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().exists("Content-Disposition"));

        verify(invoiceService, times(1)).getInvoiceById(1L);
        verify(invoiceService, times(1)).generateInvoicePdf(1L);
    }

    @Test
    @DisplayName("POST /api/invoices/batch/pdf - Should download combined PDF")
    void downloadCombinedPdf_ShouldReturnPdf() throws Exception {
        byte[] pdfBytes = "Combined PDF content".getBytes();
        List<Long> invoiceIds = Arrays.asList(1L, 2L);
        when(invoiceService.generateCombinedPdf(invoiceIds)).thenReturn(pdfBytes);

        mockMvc.perform(post("/api/invoices/batch/pdf")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invoiceIds)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().exists("Content-Disposition"));

        verify(invoiceService, times(1)).generateCombinedPdf(invoiceIds);
    }

    @Test
    @DisplayName("POST /api/invoices/batch/pdf - Should return bad request for empty list")
    void downloadCombinedPdf_WithEmptyList_ShouldReturnBadRequest() throws Exception {
        List<Long> emptyList = List.of();

        mockMvc.perform(post("/api/invoices/batch/pdf")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyList)))
                .andExpect(status().isBadRequest());

        verify(invoiceService, never()).generateCombinedPdf(any());
    }

    @Test
    @DisplayName("GET /api/invoices - Should return empty list when no invoices")
    void getAllInvoices_WhenNoInvoices_ShouldReturnEmptyList() throws Exception {
        when(invoiceService.getAllInvoices()).thenReturn(List.of());

        mockMvc.perform(get("/api/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(invoiceService, times(1)).getAllInvoices();
    }
}
