package com.hansal.verrechnungsprogramm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "meat_cut_id")
    private MeatCut meatCut;

    @Column(name = "quantity")
    private Integer quantity;

    @NotNull(message = "Weight is required")
    @Column(name = "weight", nullable = false, precision = 10, scale = 3)
    private BigDecimal weight;

    @NotNull(message = "Unit price is required")
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @PrePersist
    @PreUpdate
    protected void calculateSubtotal() {
        if (weight != null && unitPrice != null) {
            // Weight is in kg, unitPrice is per kg
            // Calculate: weight_in_kg * price_per_kg = total
            subtotal = unitPrice.multiply(weight).setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }

    public BigDecimal getSubtotal() {
        if (subtotal == null) {
            calculateSubtotal();
        }
        return subtotal;
    }

    public String getItemName() {
        if (meatCut != null && meatCut.getProduct() != null) {
            return meatCut.getProduct().getName();
        } else if (product != null) {
            return product.getName();
        }
        return "Unknown";
    }
}
