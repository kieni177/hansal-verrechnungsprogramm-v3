package com.hansal.verrechnungsprogramm.controller;

import com.hansal.verrechnungsprogramm.dto.ProductWithStockDTO;
import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.debug("GET /api/products");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        log.debug("GET /api/products/{}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        log.debug("GET /api/products/search?name={}", name);
        return ResponseEntity.ok(productService.searchProducts(name));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        log.debug("POST /api/products - name: {}", product.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(product));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<Product>> createProducts(@Valid @RequestBody List<Product> products) {
        log.debug("POST /api/products/bulk - {} products", products.size());
        List<Product> createdProducts = products.stream()
                .map(productService::createProduct)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProducts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody Product product) {
        log.debug("PUT /api/products/{}", id);
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.debug("DELETE /api/products/{}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/with-stock")
    public ResponseEntity<List<ProductWithStockDTO>> getAllProductsWithStock() {
        log.debug("GET /api/products/with-stock");
        List<Product> products = productService.getAllProducts();
        List<ProductWithStockDTO> productsWithStock = products.stream()
                .map(product -> {
                    BigDecimal stock = productService.getAvailableStock(product);
                    return ProductWithStockDTO.fromProduct(product, stock);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(productsWithStock);
    }

    @GetMapping("/{id}/with-stock")
    public ResponseEntity<ProductWithStockDTO> getProductWithStock(@PathVariable Long id) {
        log.debug("GET /api/products/{}/with-stock", id);
        Product product = productService.getProductById(id);
        BigDecimal stock = productService.getAvailableStock(product);
        return ResponseEntity.ok(ProductWithStockDTO.fromProduct(product, stock));
    }

    @GetMapping("/{id}/available-stock")
    public ResponseEntity<BigDecimal> getProductAvailableStock(@PathVariable Long id) {
        log.debug("GET /api/products/{}/available-stock", id);
        return ResponseEntity.ok(productService.getAvailableStockById(id));
    }

    @PostMapping("/init-defaults")
    public ResponseEntity<List<Product>> initializeDefaultProducts() {
        log.info("POST /api/products/init-defaults - initializing default products");
        return ResponseEntity.ok(productService.initializeDefaultProducts());
    }
}
