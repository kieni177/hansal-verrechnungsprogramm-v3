package com.hansal.verrechnungsprogramm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "slaughters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Slaughter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Cow tag is required")
    @Column(name = "cow_tag", nullable = false)
    private String cowTag;

    @Column(name = "cow_id")
    private String cowId;

    @NotNull(message = "Slaughter date is required")
    @Column(name = "slaughter_date", nullable = false)
    private LocalDate slaughterDate;

    @Column(name = "total_weight", precision = 10, scale = 2)
    private BigDecimal totalWeight;

    @OneToMany(mappedBy = "slaughter", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MeatCut> meatCuts = new ArrayList<>();

    @Column(length = 2000)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateTotalWeight();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotalWeight();
    }

    private void calculateTotalWeight() {
        if (meatCuts != null && !meatCuts.isEmpty()) {
            totalWeight = meatCuts.stream()
                    .map(MeatCut::getTotalWeight)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            totalWeight = BigDecimal.ZERO;
        }
    }

    public void addMeatCut(MeatCut meatCut) {
        meatCuts.add(meatCut);
        meatCut.setSlaughter(this);
    }

    public void removeMeatCut(MeatCut meatCut) {
        meatCuts.remove(meatCut);
        meatCut.setSlaughter(null);
    }
}
