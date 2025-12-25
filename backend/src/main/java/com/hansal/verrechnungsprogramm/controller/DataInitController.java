package com.hansal.verrechnungsprogramm.controller;

import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.service.DataInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DataInitController {

    private final DataInitService dataInitService;

    @GetMapping("/products/default")
    public ResponseEntity<List<Product>> getDefaultProducts() {
        log.debug("GET /api/init/products/default");
        return ResponseEntity.ok(dataInitService.getDefaultProducts());
    }

    @PostMapping("/products")
    public ResponseEntity<Map<String, Object>> initializeProducts(
            @RequestParam(defaultValue = "false") boolean overwrite) {
        log.info("POST /api/init/products - overwrite: {}", overwrite);
        List<Product> createdProducts = dataInitService.initializeDefaultProducts(overwrite);
        log.info("Initialized {} products", createdProducts.size());

        return ResponseEntity.ok(Map.of(
                "message", "Products initialized successfully",
                "count", createdProducts.size(),
                "products", createdProducts
        ));
    }

    @PostMapping("/products/reset")
    public ResponseEntity<Map<String, Object>> resetProducts() {
        log.warn("POST /api/init/products/reset - resetting all products");
        List<Product> products = dataInitService.resetProducts();
        log.info("Reset products - {} products created", products.size());

        return ResponseEntity.ok(Map.of(
                "message", "Products reset successfully. All existing products deleted and default products created.",
                "count", products.size(),
                "products", products
        ));
    }

    @DeleteMapping("/products/clear")
    public ResponseEntity<Map<String, String>> clearAllProducts() {
        log.warn("DELETE /api/init/products/clear - clearing all products");
        dataInitService.clearAllProducts();
        log.info("All products cleared");

        return ResponseEntity.ok(Map.of(
                "message", "All products cleared successfully"
        ));
    }
}
