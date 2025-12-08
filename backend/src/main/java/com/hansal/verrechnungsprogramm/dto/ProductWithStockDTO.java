package com.hansal.verrechnungsprogramm.dto;

import com.hansal.verrechnungsprogramm.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductWithStockDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String meatCutType;
    private BigDecimal manualStockQuantity;
    private BigDecimal availableStock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductWithStockDTO fromProduct(Product product, BigDecimal availableStock) {
        ProductWithStockDTO dto = new ProductWithStockDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setMeatCutType(product.getMeatCutType());
        dto.setManualStockQuantity(product.getStockQuantity());
        dto.setAvailableStock(availableStock);
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
}
