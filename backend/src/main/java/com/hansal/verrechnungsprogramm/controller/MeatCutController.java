package com.hansal.verrechnungsprogramm.controller;

import com.hansal.verrechnungsprogramm.dto.MeatCutAvailabilityDTO;
import com.hansal.verrechnungsprogramm.model.MeatCut;
import com.hansal.verrechnungsprogramm.service.MeatCutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/meat-cuts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MeatCutController {

    private final MeatCutService meatCutService;

    @GetMapping
    public ResponseEntity<List<MeatCut>> getAllMeatCuts() {
        log.debug("GET /api/meat-cuts");
        return ResponseEntity.ok(meatCutService.getAllMeatCuts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeatCut> getMeatCutById(@PathVariable Long id) {
        log.debug("GET /api/meat-cuts/{}", id);
        return ResponseEntity.ok(meatCutService.getMeatCutById(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<MeatCut>> getAvailableMeatCuts() {
        log.debug("GET /api/meat-cuts/available");
        return ResponseEntity.ok(meatCutService.getAvailableMeatCuts());
    }

    @GetMapping("/slaughter/{slaughterId}")
    public ResponseEntity<List<MeatCut>> getMeatCutsBySlaughter(@PathVariable Long slaughterId) {
        log.debug("GET /api/meat-cuts/slaughter/{}", slaughterId);
        return ResponseEntity.ok(meatCutService.getMeatCutsBySlaughter(slaughterId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<MeatCut>> searchMeatCuts(
            @RequestParam Long productId,
            @RequestParam BigDecimal minWeight) {
        log.debug("GET /api/meat-cuts/search?productId={}&minWeight={}", productId, minWeight);
        return ResponseEntity.ok(meatCutService.searchMeatCuts(productId, minWeight));
    }

    @GetMapping("/availability/product/{productId}")
    public ResponseEntity<List<MeatCutAvailabilityDTO>> getAvailabilityByProduct(@PathVariable Long productId) {
        log.debug("GET /api/meat-cuts/availability/product/{}", productId);
        return ResponseEntity.ok(meatCutService.getAvailabilityByProduct(productId));
    }
}
