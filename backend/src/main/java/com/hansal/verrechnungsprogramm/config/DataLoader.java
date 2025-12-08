package com.hansal.verrechnungsprogramm.config;

import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only load data if the database is empty
        if (productRepository.count() == 0) {
            loadDefaultProducts();
        }
    }

    private void loadDefaultProducts() {
        // Beef Products
        productRepository.save(createProduct(
            "Bio-Rindfleisch - Filet",
            "Zartes Rinderfilet aus biologischer Aufzucht, erstklassige Qualität",
            new BigDecimal("45.00"),
            "Rind"
        ));

        productRepository.save(createProduct(
            "Bio-Rindfleisch - Entrecôte",
            "Saftiges Entrecôte mit feiner Marmorierung",
            new BigDecimal("38.00"),
            "Rind"
        ));

        productRepository.save(createProduct(
            "Bio-Rindfleisch - Tafelspitz",
            "Klassischer Tafelspitz für traditionelle Gerichte",
            new BigDecimal("28.00"),
            "Rind"
        ));

        productRepository.save(createProduct(
            "Bio-Rindfleisch - Gulasch",
            "Hochwertiges Gulaschfleisch, perfekt für Eintöpfe",
            new BigDecimal("22.00"),
            "Rind"
        ));

        productRepository.save(createProduct(
            "Bio-Rindfleisch - Hackfleisch",
            "Frisch faschiertes Rindfleisch",
            new BigDecimal("18.00"),
            "Rind"
        ));

        // Pork Products
        productRepository.save(createProduct(
            "Bio-Schweinefleisch - Schnitzel",
            "Zartes Schweineschnitzel aus artgerechter Haltung",
            new BigDecimal("16.00"),
            "Schwein"
        ));

        productRepository.save(createProduct(
            "Bio-Schweinefleisch - Karree",
            "Saftiges Schweinekarree mit Fettrand",
            new BigDecimal("18.00"),
            "Schwein"
        ));

        productRepository.save(createProduct(
            "Bio-Schweinefleisch - Bauchfleisch",
            "Aromatisches Bauchfleisch für Braten und Grill",
            new BigDecimal("14.00"),
            "Schwein"
        ));

        productRepository.save(createProduct(
            "Bio-Schweinefleisch - Bratenstück",
            "Perfekt für Schweinsbraten",
            new BigDecimal("15.00"),
            "Schwein"
        ));

        // Lamb Products
        productRepository.save(createProduct(
            "Bio-Lammfleisch - Keule",
            "Zarte Lammkeule aus Weidehaltung",
            new BigDecimal("32.00"),
            "Lamm"
        ));

        productRepository.save(createProduct(
            "Bio-Lammfleisch - Koteletts",
            "Saftige Lammkoteletts",
            new BigDecimal("35.00"),
            "Lamm"
        ));

        productRepository.save(createProduct(
            "Bio-Lammfleisch - Schulter",
            "Aromatische Lammschulter",
            new BigDecimal("28.00"),
            "Lamm"
        ));

        // Poultry Products
        productRepository.save(createProduct(
            "Bio-Hendl - Ganzes Huhn",
            "Ganzes Bio-Hendl aus Freilandhaltung (ca. 1.5 kg)",
            new BigDecimal("14.00"),
            "Geflügel"
        ));

        productRepository.save(createProduct(
            "Bio-Hendl - Brust",
            "Zarte Hühnerbrust ohne Haut",
            new BigDecimal("22.00"),
            "Geflügel"
        ));

        productRepository.save(createProduct(
            "Bio-Hendl - Schenkel",
            "Saftige Hühnerschenkel",
            new BigDecimal("12.00"),
            "Geflügel"
        ));

        // Sausages and Processed Meat
        productRepository.save(createProduct(
            "Bio-Bratwurst",
            "Würzige Bratwurst aus eigenem Fleisch",
            new BigDecimal("16.00"),
            "Wurst"
        ));

        productRepository.save(createProduct(
            "Bio-Leberkäse",
            "Hausgemachter Leberkäse nach traditionellem Rezept",
            new BigDecimal("12.00"),
            "Wurst"
        ));

        productRepository.save(createProduct(
            "Bio-Speck",
            "Geräucherter Speck aus eigener Produktion",
            new BigDecimal("24.00"),
            "Speck"
        ));

        productRepository.save(createProduct(
            "Bio-Selchwurst",
            "Traditionelle geselchte Wurst",
            new BigDecimal("18.00"),
            "Wurst"
        ));

        // Specialty Products
        productRepository.save(createProduct(
            "Bio-Eier",
            "Frische Eier aus Freilandhaltung (10 Stück pro kg)",
            new BigDecimal("4.50"),
            "Eier"
        ));

        productRepository.save(createProduct(
            "Bio-Honig",
            "Blütenhonig aus eigener Imkerei",
            new BigDecimal("15.00"),
            "Honig"
        ));

        productRepository.save(createProduct(
            "Bio-Schmalz",
            "Reines Schweineschmalz",
            new BigDecimal("8.00"),
            "Fett"
        ));

        System.out.println("✓ Default products loaded successfully!");
    }

    private Product createProduct(String name, String description, BigDecimal pricePerKg, String meatCutType) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(pricePerKg);
        product.setMeatCutType(meatCutType);
        product.setStockQuantity(BigDecimal.ZERO); // No stock tracking by default
        return product;
    }
}
