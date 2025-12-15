package com.hansal.verrechnungsprogramm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hansal.verrechnungsprogramm.dto.ProductWithStockDTO;
import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.service.ProductService;
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

@WebMvcTest(ProductController.class)
@WithMockUser
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private Product testProduct;
    private List<Product> testProducts;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Rindfleisch");
        testProduct.setDescription("Premium Rindfleisch");
        testProduct.setPrice(new BigDecimal("25.00"));
        testProduct.setMeatCutType("beef");
        testProduct.setStockQuantity(new BigDecimal("100.00"));
        testProduct.setCreatedAt(LocalDateTime.now());
        testProduct.setUpdatedAt(LocalDateTime.now());

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Schweinefleisch");
        product2.setDescription("Premium Schweinefleisch");
        product2.setPrice(new BigDecimal("18.00"));
        product2.setMeatCutType("pork");
        product2.setStockQuantity(new BigDecimal("150.00"));

        testProducts = Arrays.asList(testProduct, product2);
    }

    @Test
    @DisplayName("GET /api/products - Should return all products")
    void getAllProducts_ShouldReturnAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(testProducts);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Rindfleisch")))
                .andExpect(jsonPath("$[1].name", is("Schweinefleisch")));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return product by ID")
    void getProductById_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(testProduct);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Rindfleisch")))
                .andExpect(jsonPath("$.price", is(25.00)));

        verify(productService, times(1)).getProductById(1L);
    }

    @Test
    @DisplayName("GET /api/products/search - Should search products by name")
    void searchProducts_ShouldReturnMatchingProducts() throws Exception {
        when(productService.searchProducts("Rind")).thenReturn(List.of(testProduct));

        mockMvc.perform(get("/api/products/search")
                        .param("name", "Rind"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Rindfleisch")));

        verify(productService, times(1)).searchProducts("Rind");
    }

    @Test
    @DisplayName("POST /api/products - Should create new product")
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        Product newProduct = new Product();
        newProduct.setName("Rindfleisch");
        newProduct.setPrice(new BigDecimal("25.00"));

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Rindfleisch")));

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    @DisplayName("POST /api/products/bulk - Should create multiple products")
    void createProducts_ShouldReturnCreatedProducts() throws Exception {
        when(productService.createProduct(any(Product.class)))
                .thenReturn(testProduct)
                .thenReturn(testProducts.get(1));

        mockMvc.perform(post("/api/products/bulk")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProducts)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(productService, times(2)).createProduct(any(Product.class));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Should update product")
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Rindfleisch");
        updatedProduct.setPrice(new BigDecimal("30.00"));

        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        mockMvc.perform(put("/api/products/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Rindfleisch")))
                .andExpect(jsonPath("$.price", is(30.00)));

        verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Should delete product")
    void deleteProduct_ShouldReturnNoContent() throws Exception {
        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    @DisplayName("GET /api/products/with-stock - Should return all products with stock")
    void getAllProductsWithStock_ShouldReturnProductsWithStock() throws Exception {
        when(productService.getAllProducts()).thenReturn(testProducts);
        when(productService.getAvailableStock(any(Product.class)))
                .thenReturn(new BigDecimal("50.00"));

        mockMvc.perform(get("/api/products/with-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].availableStock", is(50.00)));

        verify(productService, times(1)).getAllProducts();
        verify(productService, times(2)).getAvailableStock(any(Product.class));
    }

    @Test
    @DisplayName("GET /api/products/{id}/with-stock - Should return product with stock")
    void getProductWithStock_ShouldReturnProductWithStock() throws Exception {
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(productService.getAvailableStock(testProduct)).thenReturn(new BigDecimal("75.00"));

        mockMvc.perform(get("/api/products/1/with-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Rindfleisch")))
                .andExpect(jsonPath("$.availableStock", is(75.00)));

        verify(productService, times(1)).getProductById(1L);
        verify(productService, times(1)).getAvailableStock(testProduct);
    }

    @Test
    @DisplayName("GET /api/products/{id}/available-stock - Should return available stock")
    void getProductAvailableStock_ShouldReturnStock() throws Exception {
        when(productService.getAvailableStockById(1L)).thenReturn(new BigDecimal("100.00"));

        mockMvc.perform(get("/api/products/1/available-stock"))
                .andExpect(status().isOk())
                .andExpect(content().string("100.00"));

        verify(productService, times(1)).getAvailableStockById(1L);
    }

    @Test
    @DisplayName("POST /api/products/init-defaults - Should initialize default products")
    void initializeDefaultProducts_ShouldReturnProducts() throws Exception {
        when(productService.initializeDefaultProducts()).thenReturn(testProducts);

        mockMvc.perform(post("/api/products/init-defaults")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(productService, times(1)).initializeDefaultProducts();
    }
}
