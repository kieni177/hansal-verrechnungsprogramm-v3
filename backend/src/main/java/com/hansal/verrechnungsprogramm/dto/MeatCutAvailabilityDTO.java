package com.hansal.verrechnungsprogramm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeatCutAvailabilityDTO {
    private Long meatCutId;
    private String cowTag;
    private String cowId;
    private LocalDate slaughterDate;
    private BigDecimal availableWeight;
    private BigDecimal totalWeight;
    private BigDecimal pricePerKg;
    private String productName;
}
