package com.hansal.verrechnungsprogramm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.service.DataInitService;
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
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataInitController.class)
@WithMockUser
class DataInitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DataInitService dataInitService;

    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Rindfleisch");
        product1.setPrice(new BigDecimal("25.00"));

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Schweinefleisch");
        product2.setPrice(new BigDecimal("18.00"));

        testProducts = Arrays.asList(product1, product2);
    }

    @Test
    @DisplayName("GET /api/init/products/default - Should return default products")
    void getDefaultProducts_ShouldReturnDefaultProducts() throws Exception {
        when(dataInitService.getDefaultProducts()).thenReturn(testProducts);

        mockMvc.perform(get("/api/init/products/default"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Rindfleisch")))
                .andExpect(jsonPath("$[1].name", is("Schweinefleisch")));

        verify(dataInitService, times(1)).getDefaultProducts();
    }

    @Test
    @DisplayName("POST /api/init/products - Should initialize products without overwrite")
    void initializeProducts_WithoutOverwrite_ShouldReturnProducts() throws Exception {
        when(dataInitService.initializeDefaultProducts(false)).thenReturn(testProducts);

        mockMvc.perform(post("/api/init/products")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Products initialized successfully")))
                .andExpect(jsonPath("$.count", is(2)))
                .andExpect(jsonPath("$.products", hasSize(2)));

        verify(dataInitService, times(1)).initializeDefaultProducts(false);
    }

    @Test
    @DisplayName("POST /api/init/products - Should initialize products with overwrite")
    void initializeProducts_WithOverwrite_ShouldReturnProducts() throws Exception {
        when(dataInitService.initializeDefaultProducts(true)).thenReturn(testProducts);

        mockMvc.perform(post("/api/init/products")
                        .with(csrf())
                        .param("overwrite", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Products initialized successfully")))
                .andExpect(jsonPath("$.count", is(2)));

        verify(dataInitService, times(1)).initializeDefaultProducts(true);
    }

    @Test
    @DisplayName("POST /api/init/products/reset - Should reset products")
    void resetProducts_ShouldReturnResetProducts() throws Exception {
        when(dataInitService.resetProducts()).thenReturn(testProducts);

        mockMvc.perform(post("/api/init/products/reset")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Products reset successfully")))
                .andExpect(jsonPath("$.count", is(2)))
                .andExpect(jsonPath("$.products", hasSize(2)));

        verify(dataInitService, times(1)).resetProducts();
    }

    @Test
    @DisplayName("DELETE /api/init/products/clear - Should clear all products")
    void clearAllProducts_ShouldReturnSuccess() throws Exception {
        doNothing().when(dataInitService).clearAllProducts();

        mockMvc.perform(delete("/api/init/products/clear")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("All products cleared successfully")));

        verify(dataInitService, times(1)).clearAllProducts();
    }

    @Test
    @DisplayName("GET /api/init/products/default - Should return empty list when no defaults")
    void getDefaultProducts_WhenNoDefaults_ShouldReturnEmptyList() throws Exception {
        when(dataInitService.getDefaultProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/init/products/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(dataInitService, times(1)).getDefaultProducts();
    }

    @Test
    @DisplayName("POST /api/init/products - Should return empty when no products created")
    void initializeProducts_WhenNoProductsCreated_ShouldReturnEmptyList() throws Exception {
        when(dataInitService.initializeDefaultProducts(anyBoolean())).thenReturn(List.of());

        mockMvc.perform(post("/api/init/products")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(0)))
                .andExpect(jsonPath("$.products", hasSize(0)));

        verify(dataInitService, times(1)).initializeDefaultProducts(false);
    }

    @Test
    @DisplayName("POST /api/init/products/reset - Should return empty when no products after reset")
    void resetProducts_WhenNoProducts_ShouldReturnEmptyList() throws Exception {
        when(dataInitService.resetProducts()).thenReturn(List.of());

        mockMvc.perform(post("/api/init/products/reset")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count", is(0)));

        verify(dataInitService, times(1)).resetProducts();
    }
}
