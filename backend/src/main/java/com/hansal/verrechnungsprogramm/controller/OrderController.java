package com.hansal.verrechnungsprogramm.controller;

import com.hansal.verrechnungsprogramm.dto.CustomerDTO;
import com.hansal.verrechnungsprogramm.model.Order;
import com.hansal.verrechnungsprogramm.model.OrderStatus;
import com.hansal.verrechnungsprogramm.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        log.debug("GET /api/orders");
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerDTO>> getUniqueCustomers() {
        log.debug("GET /api/orders/customers");
        return ResponseEntity.ok(orderService.getUniqueCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        log.debug("GET /api/orders/{}", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Order>> searchOrders(@RequestParam String customerName) {
        log.debug("GET /api/orders/search?customerName={}", customerName);
        return ResponseEntity.ok(orderService.searchOrdersByCustomerName(customerName));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.debug("GET /api/orders/status/{}", status);
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        log.debug("POST /api/orders - customer: {}", order.getCustomerName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(order));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody Order order) {
        log.debug("PUT /api/orders/{}", id);
        return ResponseEntity.ok(orderService.updateOrder(id, order));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {
        log.debug("PATCH /api/orders/{}/status - {}", id, status);
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        log.debug("DELETE /api/orders/{}", id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
