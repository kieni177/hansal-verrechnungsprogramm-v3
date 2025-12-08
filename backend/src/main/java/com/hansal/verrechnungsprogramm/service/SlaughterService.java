package com.hansal.verrechnungsprogramm.service;

import com.hansal.verrechnungsprogramm.model.MeatCut;
import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.model.Slaughter;
import com.hansal.verrechnungsprogramm.repository.SlaughterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SlaughterService {

    private final SlaughterRepository slaughterRepository;
    private final ProductService productService;

    public List<Slaughter> getAllSlaughters() {
        return slaughterRepository.findAll();
    }

    public Slaughter getSlaughterById(Long id) {
        return slaughterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slaughter record not found with id: " + id));
    }

    public List<Slaughter> searchByCowTag(String cowTag) {
        return slaughterRepository.findByCowTagContainingIgnoreCase(cowTag);
    }

    public List<Slaughter> getSlaughtersByDateRange(LocalDate startDate, LocalDate endDate) {
        return slaughterRepository.findBySlaughterDateBetween(startDate, endDate);
    }

    public Slaughter createSlaughter(Slaughter slaughter) {
        // Set bidirectional relationship for meat cuts and update product stock
        if (slaughter.getMeatCuts() != null) {
            slaughter.getMeatCuts().forEach(meatCut -> {
                meatCut.setSlaughter(slaughter);

                // Update product stock based on total weight (using BigDecimal for precision)
                if (meatCut.getProduct() != null && meatCut.getTotalWeight() != null) {
                    Product product = meatCut.getProduct();
                    BigDecimal weight = meatCut.getTotalWeight();

                    BigDecimal newStock;
                    if (product.getStockQuantity() == null) {
                        newStock = weight;
                    } else {
                        newStock = product.getStockQuantity().add(weight);
                    }
                    productService.updateProductStock(product.getId(), newStock);
                }
            });
        }
        return slaughterRepository.save(slaughter);
    }

    public Slaughter updateSlaughter(Long id, Slaughter slaughterDetails) {
        Slaughter slaughter = getSlaughterById(id);

        // Remove stock from old meat cuts (using BigDecimal for precision)
        if (slaughter.getMeatCuts() != null) {
            slaughter.getMeatCuts().forEach(oldMeatCut -> {
                if (oldMeatCut.getProduct() != null && oldMeatCut.getTotalWeight() != null) {
                    Product product = oldMeatCut.getProduct();
                    BigDecimal weight = oldMeatCut.getTotalWeight();

                    if (product.getStockQuantity() != null) {
                        BigDecimal newStock = product.getStockQuantity().subtract(weight);
                        newStock = newStock.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newStock;
                        productService.updateProductStock(product.getId(), newStock);
                    }
                }
            });
        }

        slaughter.setCowTag(slaughterDetails.getCowTag());
        slaughter.setCowId(slaughterDetails.getCowId());
        slaughter.setSlaughterDate(slaughterDetails.getSlaughterDate());
        slaughter.setNotes(slaughterDetails.getNotes());

        // Update meat cuts with bidirectional relationship and add stock
        slaughter.getMeatCuts().clear();
        if (slaughterDetails.getMeatCuts() != null) {
            slaughterDetails.getMeatCuts().forEach(meatCut -> {
                meatCut.setSlaughter(slaughter);
                slaughter.getMeatCuts().add(meatCut);

                // Add stock for new meat cuts (using BigDecimal for precision)
                if (meatCut.getProduct() != null && meatCut.getTotalWeight() != null) {
                    Product product = meatCut.getProduct();
                    BigDecimal weight = meatCut.getTotalWeight();

                    BigDecimal newStock;
                    if (product.getStockQuantity() == null) {
                        newStock = weight;
                    } else {
                        newStock = product.getStockQuantity().add(weight);
                    }
                    productService.updateProductStock(product.getId(), newStock);
                }
            });
        }

        return slaughterRepository.save(slaughter);
    }

    public void deleteSlaughter(Long id) {
        Slaughter slaughter = getSlaughterById(id);

        // Remove stock from products when deleting slaughter (using BigDecimal for precision)
        if (slaughter.getMeatCuts() != null) {
            slaughter.getMeatCuts().forEach(meatCut -> {
                if (meatCut.getProduct() != null && meatCut.getTotalWeight() != null) {
                    Product product = meatCut.getProduct();
                    BigDecimal weight = meatCut.getTotalWeight();

                    if (product.getStockQuantity() != null) {
                        BigDecimal newStock = product.getStockQuantity().subtract(weight);
                        newStock = newStock.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newStock;
                        productService.updateProductStock(product.getId(), newStock);
                    }
                }
            });
        }

        slaughterRepository.delete(slaughter);
    }
}
