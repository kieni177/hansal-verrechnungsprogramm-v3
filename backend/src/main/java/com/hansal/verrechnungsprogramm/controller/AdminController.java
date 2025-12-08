package com.hansal.verrechnungsprogramm.controller;

import com.hansal.verrechnungsprogramm.service.DatabaseResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private final DatabaseResetService databaseResetService;

    @PostMapping("/reset-database")
    public ResponseEntity<Map<String, Object>> resetDatabase() {
        try {
            Map<String, Integer> result = databaseResetService.resetDatabase();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Database reset successfully");
            response.put("productsLoaded", result.get("productsLoaded"));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error resetting database: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
