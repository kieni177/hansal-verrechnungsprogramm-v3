package com.hansal.verrechnungsprogramm.service;

import com.hansal.verrechnungsprogramm.model.MeatCut;
import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.repository.MeatCutRepository;
import com.hansal.verrechnungsprogramm.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final MeatCutRepository meatCutRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public Product createProduct(Product product) {
        // Initialize stock to 0 - it will be updated only through slaughters
        product.setStockQuantity(BigDecimal.ZERO);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setImageUrl(productDetails.getImageUrl());
        product.setMeatCutType(productDetails.getMeatCutType());
        // Stock quantity is NOT updated here - it's managed only through slaughters
        // Use updateProductStock() instead for stock changes
        return productRepository.save(product);
    }

    /**
     * Updates the stock quantity for a product.
     * This method should ONLY be called from slaughter operations.
     */
    public Product updateProductStock(Long id, BigDecimal newStockQuantity) {
        Product product = getProductById(id);
        product.setStockQuantity(newStockQuantity);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    public BigDecimal getAvailableStock(Product product) {
        if (product.getId() != null) {
            // Calculate stock from meat cuts - sum all available weights for this product
            List<MeatCut> meatCuts = meatCutRepository.findByProductId(product.getId());
            return meatCuts.stream()
                    .map(MeatCut::getAvailableWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            // Use manual stock quantity
            return product.getStockQuantity() != null ? product.getStockQuantity() : BigDecimal.ZERO;
        }
    }

    public BigDecimal getAvailableStockById(Long id) {
        Product product = getProductById(id);
        return getAvailableStock(product);
    }

    /**
     * Resets all products and initializes with default products.
     * WARNING: This deletes all existing products!
     */
    public List<Product> initializeDefaultProducts() {
        // Delete all existing products
        productRepository.deleteAll();

        // Create default products
        List<Product> defaultProducts = new ArrayList<>();

        // Bio-Rindfleisch
        defaultProducts.add(createDefaultProduct("Bio-Rindfleisch - Filet", "Zartes Rinderfilet aus biologischer Aufzucht, erstklassige Qualität", new BigDecimal("45.00"), "Rind"));
        defaultProducts.add(createDefaultProduct("Bio-Rindfleisch - Entrecôte", "Saftiges Entrecôte mit feiner Marmorierung", new BigDecimal("38.00"), "Rind"));
        defaultProducts.add(createDefaultProduct("Bio-Rindfleisch - Tafelspitz", "Klassischer Tafelspitz für traditionelle Gerichte", new BigDecimal("28.00"), "Rind"));
        defaultProducts.add(createDefaultProduct("Bio-Rindfleisch - Gulasch", "Hochwertiges Gulaschfleisch, perfekt für Eintöpfe", new BigDecimal("22.00"), "Rind"));
        defaultProducts.add(createDefaultProduct("Bio-Rindfleisch - Hackfleisch", "Frisch faschiertes Rindfleisch", new BigDecimal("18.00"), "Rind"));

        // Bio-Schweinefleisch
        defaultProducts.add(createDefaultProduct("Bio-Schweinefleisch - Schnitzel", "Zartes Schweineschnitzel aus artgerechter Haltung", new BigDecimal("16.00"), "Schwein"));
        defaultProducts.add(createDefaultProduct("Bio-Schweinefleisch - Karree", "Saftiges Schweinekarree mit Fettrand", new BigDecimal("18.00"), "Schwein"));
        defaultProducts.add(createDefaultProduct("Bio-Schweinefleisch - Bauchfleisch", "Aromatisches Bauchfleisch für Braten und Grill", new BigDecimal("14.00"), "Schwein"));
        defaultProducts.add(createDefaultProduct("Bio-Schweinefleisch - Bratenstück", "Perfekt für Schweinsbraten", new BigDecimal("15.00"), "Schwein"));

        // Bio-Lammfleisch
        defaultProducts.add(createDefaultProduct("Bio-Lammfleisch - Keule", "Zarte Lammkeule aus Weidehaltung", new BigDecimal("32.00"), "Lamm"));
        defaultProducts.add(createDefaultProduct("Bio-Lammfleisch - Koteletts", "Saftige Lammkoteletts", new BigDecimal("35.00"), "Lamm"));
        defaultProducts.add(createDefaultProduct("Bio-Lammfleisch - Schulter", "Aromatische Lammschulter", new BigDecimal("28.00"), "Lamm"));

        // Bio-Hendl
        defaultProducts.add(createDefaultProduct("Bio-Hendl - Ganzes Huhn", "Ganzes Bio-Hendl aus Freilandhaltung (ca. 1.5 kg)", new BigDecimal("14.00"), "Geflügel"));
        defaultProducts.add(createDefaultProduct("Bio-Hendl - Brust", "Zarte Hühnerbrust ohne Haut", new BigDecimal("22.00"), "Geflügel"));
        defaultProducts.add(createDefaultProduct("Bio-Hendl - Schenkel", "Saftige Hühnerschenkel", new BigDecimal("12.00"), "Geflügel"));

        // Wurst & Speck
        defaultProducts.add(createDefaultProduct("Bio-Bratwurst", "Würzige Bratwurst aus eigenem Fleisch", new BigDecimal("16.00"), "Wurst"));
        defaultProducts.add(createDefaultProduct("Bio-Leberkäse", "Hausgemachter Leberkäse nach traditionellem Rezept", new BigDecimal("12.00"), "Wurst"));
        defaultProducts.add(createDefaultProduct("Bio-Speck", "Geräucherter Speck aus eigener Produktion", new BigDecimal("24.00"), "Speck"));
        defaultProducts.add(createDefaultProduct("Bio-Selchwurst", "Traditionelle geselchte Wurst", new BigDecimal("18.00"), "Wurst"));

        // Andere Produkte
        defaultProducts.add(createDefaultProduct("Bio-Eier", "Frische Eier aus Freilandhaltung (10 Stück pro kg)", new BigDecimal("4.50"), "Eier"));
        defaultProducts.add(createDefaultProduct("Bio-Honig", "Blütenhonig aus eigener Imkerei", new BigDecimal("15.00"), "Honig"));
        defaultProducts.add(createDefaultProduct("Bio-Schmalz", "Reines Schweineschmalz", new BigDecimal("8.00"), "Fett"));

        // Save all products
        return productRepository.saveAll(defaultProducts);
    }

    private Product createDefaultProduct(String name, String description, BigDecimal price, String meatCutType) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setMeatCutType(meatCutType);
        product.setStockQuantity(BigDecimal.ZERO);
        return product;
    }
}
