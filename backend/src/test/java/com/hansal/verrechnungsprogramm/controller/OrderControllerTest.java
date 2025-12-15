package com.hansal.verrechnungsprogramm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hansal.verrechnungsprogramm.dto.CustomerDTO;
import com.hansal.verrechnungsprogramm.model.Order;
import com.hansal.verrechnungsprogramm.model.OrderStatus;
import com.hansal.verrechnungsprogramm.service.OrderService;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@WithMockUser
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private Order testOrder;
    private List<Order> testOrders;
    private CustomerDTO testCustomer;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCustomerName("Max Mustermann");
        testOrder.setCustomerPhone("+43 660 1234567");
        testOrder.setCustomerAddress("Musterstraße 1, 1010 Wien");
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("150.00"));
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());

        Order order2 = new Order();
        order2.setId(2L);
        order2.setCustomerName("Erika Mustermann");
        order2.setCustomerPhone("+43 660 7654321");
        order2.setStatus(OrderStatus.COMPLETED);
        order2.setTotalAmount(new BigDecimal("200.00"));
        order2.setOrderDate(LocalDateTime.now());

        testOrders = Arrays.asList(testOrder, order2);

        testCustomer = new CustomerDTO();
        testCustomer.setName("Max Mustermann");
        testCustomer.setPhone("+43 660 1234567");
        testCustomer.setAddress("Musterstraße 1, 1010 Wien");
        testCustomer.setLastOrderDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /api/orders - Should return all orders")
    void getAllOrders_ShouldReturnAllOrders() throws Exception {
        when(orderService.getAllOrders()).thenReturn(testOrders);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerName", is("Max Mustermann")))
                .andExpect(jsonPath("$[1].customerName", is("Erika Mustermann")));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    @DisplayName("GET /api/orders/customers - Should return unique customers")
    void getUniqueCustomers_ShouldReturnCustomers() throws Exception {
        when(orderService.getUniqueCustomers()).thenReturn(List.of(testCustomer));

        mockMvc.perform(get("/api/orders/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Max Mustermann")))
                .andExpect(jsonPath("$[0].phone", is("+43 660 1234567")));

        verify(orderService, times(1)).getUniqueCustomers();
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Should return order by ID")
    void getOrderById_ShouldReturnOrder() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(testOrder);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customerName", is("Max Mustermann")))
                .andExpect(jsonPath("$.status", is("PENDING")));

        verify(orderService, times(1)).getOrderById(1L);
    }

    @Test
    @DisplayName("GET /api/orders/search - Should search orders by customer name")
    void searchOrders_ShouldReturnMatchingOrders() throws Exception {
        when(orderService.searchOrdersByCustomerName("Max")).thenReturn(List.of(testOrder));

        mockMvc.perform(get("/api/orders/search")
                        .param("customerName", "Max"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerName", is("Max Mustermann")));

        verify(orderService, times(1)).searchOrdersByCustomerName("Max");
    }

    @Test
    @DisplayName("GET /api/orders/status/{status} - Should return orders by status")
    void getOrdersByStatus_ShouldReturnOrdersWithStatus() throws Exception {
        when(orderService.getOrdersByStatus(OrderStatus.PENDING)).thenReturn(List.of(testOrder));

        mockMvc.perform(get("/api/orders/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));

        verify(orderService, times(1)).getOrdersByStatus(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("POST /api/orders - Should create new order")
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        when(orderService.createOrder(any(Order.class))).thenReturn(testOrder);

        Order newOrder = new Order();
        newOrder.setCustomerName("Max Mustermann");
        newOrder.setCustomerPhone("+43 660 1234567");

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customerName", is("Max Mustermann")));

        verify(orderService, times(1)).createOrder(any(Order.class));
    }

    @Test
    @DisplayName("PUT /api/orders/{id} - Should update order")
    void updateOrder_ShouldReturnUpdatedOrder() throws Exception {
        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setCustomerName("Max Mustermann Updated");
        updatedOrder.setCustomerPhone("+43 660 9999999");
        updatedOrder.setStatus(OrderStatus.PROCESSING);

        when(orderService.updateOrder(eq(1L), any(Order.class))).thenReturn(updatedOrder);

        mockMvc.perform(put("/api/orders/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerName", is("Max Mustermann Updated")))
                .andExpect(jsonPath("$.status", is("PROCESSING")));

        verify(orderService, times(1)).updateOrder(eq(1L), any(Order.class));
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/status - Should update order status")
    void updateOrderStatus_ShouldReturnUpdatedOrder() throws Exception {
        Order updatedOrder = new Order();
        updatedOrder.setId(1L);
        updatedOrder.setCustomerName("Max Mustermann");
        updatedOrder.setStatus(OrderStatus.COMPLETED);

        when(orderService.updateOrderStatus(1L, OrderStatus.COMPLETED)).thenReturn(updatedOrder);

        mockMvc.perform(patch("/api/orders/1/status")
                        .with(csrf())
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        verify(orderService, times(1)).updateOrderStatus(1L, OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - Should delete order")
    void deleteOrder_ShouldReturnNoContent() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/api/orders/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteOrder(1L);
    }

    @Test
    @DisplayName("GET /api/orders - Should return empty list when no orders")
    void getAllOrders_WhenNoOrders_ShouldReturnEmptyList() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(orderService, times(1)).getAllOrders();
    }
}
