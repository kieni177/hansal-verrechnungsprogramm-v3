package com.hansal.verrechnungsprogramm.service;

import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DataInitService {

    private final ProductRepository productRepository;

    public List<Product> getDefaultProducts() {
        List<Product> products = new ArrayList<>();

        products.add(createProduct("Steak", "Hochwertiges Rindersteak", 28.90, "Ribeye", 0));
        products.add(createProduct("Schopf", "Schweineschopf, ideal zum Grillen", 12.50, "Chuck", 0));
        products.add(createProduct("Filet", "Zartes Schweinefilet", 18.90, "Filet", 0));
        products.add(createProduct("Rippchen", "Saftige Schweinerippchen", 11.50, "Short Rib", 0));
        products.add(createProduct("Bauchfleisch", "Durchwachsenes Bauchfleisch", 9.90, "Brisket", 0));
        products.add(createProduct("Karree", "Mageres Schweinekotelett", 14.90, "Sirloin", 0));
        products.add(createProduct("Haxe", "Schweinshaxe", 13.90, "Round", 0));
        products.add(createProduct("Gulasch", "Rindergulasch gewürfelt", 16.50, "Stew Meat", 0));
        products.add(createProduct("Faschiertes", "Gemischtes Faschiertes (Rind & Schwein)", 8.90, "Ground Beef", 0));
        products.add(createProduct("Rostbraten", "Saftiger Rostbraten vom Rind", 24.90, "Tenderloin", 0));
        products.add(createProduct("Bratwurst", "Hausgemachte Bratwurst", 7.50, null, 80));
        products.add(createProduct("Kalbsschnitzel", "Zartes Kalbsschnitzel", 22.50, "Flank", 0));
        products.add(createProduct("Leberkäse", "Frischer Leberkäse", 6.90, null, 50));
        products.add(createProduct("Braten", "Schweinebraten", 11.90, "Chuck", 0));
        products.add(createProduct("Speck", "Geräucherter Speck", 15.90, null, 30));

        return products;
    }

    public List<Product> initializeDefaultProducts(boolean overwrite) {
        List<Product> createdProducts = new ArrayList<>();

        for (Product defaultProduct : getDefaultProducts()) {
            // Check if product already exists by name
            List<Product> existing = productRepository.findByNameContainingIgnoreCase(defaultProduct.getName());

            if (existing.isEmpty()) {
                // Product doesn't exist, create it
                Product created = productRepository.save(defaultProduct);
                createdProducts.add(created);
            } else if (overwrite) {
                // Product exists and overwrite is enabled, update the first match
                Product existingProduct = existing.get(0);
                existingProduct.setDescription(defaultProduct.getDescription());
                existingProduct.setPrice(defaultProduct.getPrice());
                existingProduct.setMeatCutType(defaultProduct.getMeatCutType());
                existingProduct.setStockQuantity(defaultProduct.getStockQuantity());
                Product updated = productRepository.save(existingProduct);
                createdProducts.add(updated);
            }
        }

        log.info("Initialized default products: count={}, overwrite={}", createdProducts.size(), overwrite);
        return createdProducts;
    }

    public void clearAllProducts() {
        productRepository.deleteAll();
        log.info("Cleared all products");
    }

    public List<Product> resetProducts() {
        clearAllProducts();
        List<Product> products = initializeDefaultProducts(false);
        log.info("Reset products: count={}", products.size());
        return products;
    }

    private Product createProduct(String name, String description, double price, String meatCutType, int stockQuantity) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(BigDecimal.valueOf(price));
        product.setMeatCutType(meatCutType);
        product.setStockQuantity(BigDecimal.valueOf(stockQuantity));
        return product;
    }
}
