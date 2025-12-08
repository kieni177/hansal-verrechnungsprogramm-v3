package com.hansal.verrechnungsprogramm.controller;

import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.service.DataInitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/init")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DataInitController {

    private final DataInitService dataInitService;

    @GetMapping("/products/default")
    public ResponseEntity<List<Product>> getDefaultProducts() {
        return ResponseEntity.ok(dataInitService.getDefaultProducts());
    }

    @PostMapping("/products")
    public ResponseEntity<Map<String, Object>> initializeProducts(
            @RequestParam(defaultValue = "false") boolean overwrite) {
        List<Product> createdProducts = dataInitService.initializeDefaultProducts(overwrite);

        return ResponseEntity.ok(Map.of(
                "message", "Products initialized successfully",
                "count", createdProducts.size(),
                "products", createdProducts
        ));
    }

    @PostMapping("/products/reset")
    public ResponseEntity<Map<String, Object>> resetProducts() {
        List<Product> products = dataInitService.resetProducts();

        return ResponseEntity.ok(Map.of(
                "message", "Products reset successfully. All existing products deleted and default products created.",
                "count", products.size(),
                "products", products
        ));
    }

    @DeleteMapping("/products/clear")
    public ResponseEntity<Map<String, String>> clearAllProducts() {
        dataInitService.clearAllProducts();

        return ResponseEntity.ok(Map.of(
                "message", "All products cleared successfully"
        ));
    }
}
