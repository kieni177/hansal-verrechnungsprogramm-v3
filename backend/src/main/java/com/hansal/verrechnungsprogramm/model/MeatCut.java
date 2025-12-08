package com.hansal.verrechnungsprogramm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "meat_cuts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeatCut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slaughter_id", nullable = false)
    @JsonIgnore
    private Slaughter slaughter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @PositiveOrZero(message = "Weight must be positive or zero")
    @Column(name = "total_weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalWeight;

    @PositiveOrZero(message = "Available weight must be positive or zero")
    @Column(name = "available_weight", nullable = false, precision = 10, scale = 2)
    private BigDecimal availableWeight;

    @Column(name = "price_per_kg", precision = 10, scale = 2)
    private BigDecimal pricePerKg;

    @PrePersist
    protected void onCreate() {
        // Initialize available weight to total weight if not set
        if (availableWeight == null) {
            availableWeight = totalWeight;
        }
    }

    public BigDecimal getReservedWeight() {
        if (totalWeight == null || availableWeight == null) {
            return BigDecimal.ZERO;
        }
        return totalWeight.subtract(availableWeight);
    }

    public boolean hasAvailableWeight(BigDecimal requiredWeight) {
        return availableWeight != null &&
               requiredWeight != null &&
               availableWeight.compareTo(requiredWeight) >= 0;
    }

    public void reserveWeight(BigDecimal weight) {
        if (!hasAvailableWeight(weight)) {
            throw new IllegalStateException("Insufficient available weight");
        }
        availableWeight = availableWeight.subtract(weight);
    }

    public void releaseWeight(BigDecimal weight) {
        availableWeight = availableWeight.add(weight);
        if (availableWeight.compareTo(totalWeight) > 0) {
            availableWeight = totalWeight;
        }
    }
}
