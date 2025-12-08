package com.hansal.verrechnungsprogramm.controller;

import com.hansal.verrechnungsprogramm.model.Slaughter;
import com.hansal.verrechnungsprogramm.service.SlaughterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slaughters")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SlaughterController {

    private final SlaughterService slaughterService;

    @GetMapping
    public ResponseEntity<List<Slaughter>> getAllSlaughters() {
        return ResponseEntity.ok(slaughterService.getAllSlaughters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Slaughter> getSlaughterById(@PathVariable Long id) {
        return ResponseEntity.ok(slaughterService.getSlaughterById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Slaughter>> searchSlaughters(@RequestParam String cowTag) {
        return ResponseEntity.ok(slaughterService.searchByCowTag(cowTag));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Slaughter>> getSlaughtersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(slaughterService.getSlaughtersByDateRange(startDate, endDate));
    }

    @PostMapping
    public ResponseEntity<Slaughter> createSlaughter(@Valid @RequestBody Slaughter slaughter) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(slaughterService.createSlaughter(slaughter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Slaughter> updateSlaughter(
            @PathVariable Long id,
            @Valid @RequestBody Slaughter slaughter) {
        return ResponseEntity.ok(slaughterService.updateSlaughter(id, slaughter));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSlaughter(@PathVariable Long id) {
        slaughterService.deleteSlaughter(id);
        return ResponseEntity.noContent().build();
    }
}
