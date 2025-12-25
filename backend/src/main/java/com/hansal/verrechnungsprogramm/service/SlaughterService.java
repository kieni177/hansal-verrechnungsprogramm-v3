package com.hansal.verrechnungsprogramm.service;

import com.hansal.verrechnungsprogramm.model.MeatCut;
import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.model.Slaughter;
import com.hansal.verrechnungsprogramm.repository.SlaughterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SlaughterService {

    private final SlaughterRepository slaughterRepository;
    private final ProductService productService;

    public List<Slaughter> getAllSlaughters() {
        List<Slaughter> slaughters = slaughterRepository.findAll();
        log.info("Listed slaughters: count={}", slaughters.size());
        return slaughters;
    }

    public Slaughter getSlaughterById(Long id) {
        Slaughter slaughter = slaughterRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Slaughter not found: id={}", id);
                    return new RuntimeException("Slaughter record not found with id: " + id);
                });
        log.info("Fetched slaughter: id={}, cowTag={}", id, slaughter.getCowTag());
        return slaughter;
    }

    public List<Slaughter> searchByCowTag(String cowTag) {
        List<Slaughter> slaughters = slaughterRepository.findByCowTagContainingIgnoreCase(cowTag);
        log.info("Searched slaughters: query='{}', count={}", cowTag, slaughters.size());
        return slaughters;
    }

    public List<Slaughter> getSlaughtersByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Slaughter> slaughters = slaughterRepository.findBySlaughterDateBetween(startDate, endDate);
        log.info("Filtered slaughters by date: range={} to {}, count={}", startDate, endDate, slaughters.size());
        return slaughters;
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
        Slaughter savedSlaughter = slaughterRepository.save(slaughter);
        int meatCutCount = savedSlaughter.getMeatCuts() != null ? savedSlaughter.getMeatCuts().size() : 0;
        log.info("Created slaughter: id={}, cowTag={}, meatCuts={}", savedSlaughter.getId(), savedSlaughter.getCowTag(), meatCutCount);
        return savedSlaughter;
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

        Slaughter savedSlaughter = slaughterRepository.save(slaughter);
        int meatCutCount = savedSlaughter.getMeatCuts() != null ? savedSlaughter.getMeatCuts().size() : 0;
        log.info("Updated slaughter: id={}, cowTag={}, meatCuts={}", savedSlaughter.getId(), savedSlaughter.getCowTag(), meatCutCount);
        return savedSlaughter;
    }

    public void deleteSlaughter(Long id) {
        Slaughter slaughter = getSlaughterById(id);
        String cowTag = slaughter.getCowTag();

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
        log.info("Deleted slaughter: id={}, cowTag={}", id, cowTag);
    }
}
