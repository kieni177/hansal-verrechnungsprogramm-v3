package com.hansal.verrechnungsprogramm.service;

import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DatabaseResetService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final MeatCutRepository meatCutRepository;

    @Transactional
    public Map<String, Integer> resetDatabase() {
        // Delete all data in correct order (respecting foreign keys)
        invoiceRepository.deleteAll();
        orderRepository.deleteAll();
        meatCutRepository.deleteAll();
        productRepository.deleteAll();

        // Reload default products
        int productsLoaded = loadDefaultProducts();

        Map<String, Integer> result = new HashMap<>();
        result.put("productsLoaded", productsLoaded);
        return result;
    }

    private int loadDefaultProducts() {
        int count = 0;

        // Beef Products
        productRepository.save(createProduct(
            "Bio-Rindfleisch - Filet",
            "Zartes Rinderfilet aus biologischer Aufzucht, erstklassige Qualität",
            new BigDecimal("45.00"),
            "Rind"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Rindfleisch - Entrecôte",
            "Saftiges Entrecôte mit feiner Marmorierung",
            new BigDecimal("38.00"),
            "Rind"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Rindfleisch - Tafelspitz",
            "Klassischer Tafelspitz für traditionelle Gerichte",
            new BigDecimal("28.00"),
            "Rind"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Rindfleisch - Gulasch",
            "Hochwertiges Gulaschfleisch, perfekt für Eintöpfe",
            new BigDecimal("22.00"),
            "Rind"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Rindfleisch - Hackfleisch",
            "Frisch faschiertes Rindfleisch",
            new BigDecimal("18.00"),
            "Rind"
        ));
        count++;

        // Pork Products
        productRepository.save(createProduct(
            "Bio-Schweinefleisch - Schnitzel",
            "Zartes Schweineschnitzel aus artgerechter Haltung",
            new BigDecimal("16.00"),
            "Schwein"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Schweinefleisch - Karree",
            "Saftiges Schweinekarree mit Fettrand",
            new BigDecimal("18.00"),
            "Schwein"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Schweinefleisch - Bauchfleisch",
            "Aromatisches Bauchfleisch für Braten und Grill",
            new BigDecimal("14.00"),
            "Schwein"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Schweinefleisch - Bratenstück",
            "Perfekt für Schweinsbraten",
            new BigDecimal("15.00"),
            "Schwein"
        ));
        count++;

        // Lamb Products
        productRepository.save(createProduct(
            "Bio-Lammfleisch - Keule",
            "Zarte Lammkeule aus Weidehaltung",
            new BigDecimal("32.00"),
            "Lamm"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Lammfleisch - Koteletts",
            "Saftige Lammkoteletts",
            new BigDecimal("35.00"),
            "Lamm"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Lammfleisch - Schulter",
            "Aromatische Lammschulter",
            new BigDecimal("28.00"),
            "Lamm"
        ));
        count++;

        // Poultry Products
        productRepository.save(createProduct(
            "Bio-Hendl - Ganzes Huhn",
            "Ganzes Bio-Hendl aus Freilandhaltung (ca. 1.5 kg)",
            new BigDecimal("14.00"),
            "Geflügel"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Hendl - Brust",
            "Zarte Hühnerbrust ohne Haut",
            new BigDecimal("22.00"),
            "Geflügel"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Hendl - Schenkel",
            "Saftige Hühnerschenkel",
            new BigDecimal("12.00"),
            "Geflügel"
        ));
        count++;

        // Sausages and Processed Meat
        productRepository.save(createProduct(
            "Bio-Bratwurst",
            "Würzige Bratwurst aus eigenem Fleisch",
            new BigDecimal("16.00"),
            "Wurst"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Leberkäse",
            "Hausgemachter Leberkäse nach traditionellem Rezept",
            new BigDecimal("12.00"),
            "Wurst"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Speck",
            "Geräucherter Speck aus eigener Produktion",
            new BigDecimal("24.00"),
            "Speck"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Selchwurst",
            "Traditionelle geselchte Wurst",
            new BigDecimal("18.00"),
            "Wurst"
        ));
        count++;

        // Specialty Products
        productRepository.save(createProduct(
            "Bio-Eier",
            "Frische Eier aus Freilandhaltung (10 Stück pro kg)",
            new BigDecimal("4.50"),
            "Eier"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Honig",
            "Blütenhonig aus eigener Imkerei",
            new BigDecimal("15.00"),
            "Honig"
        ));
        count++;

        productRepository.save(createProduct(
            "Bio-Schmalz",
            "Reines Schweineschmalz",
            new BigDecimal("8.00"),
            "Fett"
        ));
        count++;

        return count;
    }

    private Product createProduct(String name, String description, BigDecimal pricePerKg, String meatCutType) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(pricePerKg);
        product.setMeatCutType(meatCutType);
        product.setStockQuantity(BigDecimal.ZERO);
        return product;
    }
}
