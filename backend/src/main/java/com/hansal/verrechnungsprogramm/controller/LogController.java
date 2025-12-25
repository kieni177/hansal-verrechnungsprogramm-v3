package com.hansal.verrechnungsprogramm.controller;

import com.hansal.verrechnungsprogramm.dto.LogEntryDTO;
import com.hansal.verrechnungsprogramm.service.LogStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LogController {

    private final LogStorageService logStorageService;

    @GetMapping
    public ResponseEntity<List<LogEntryDTO>> getRecentLogs(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(logStorageService.getRecentLogs(limit));
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<List<LogEntryDTO>> getLogsByLevel(
            @PathVariable String level,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(logStorageService.getLogsByLevel(level, limit));
    }

    @GetMapping("/since")
    public ResponseEntity<List<LogEntryDTO>> getLogsSince(
            @RequestParam String timestamp) {
        LocalDateTime since = LocalDateTime.parse(timestamp);
        return ResponseEntity.ok(logStorageService.getLogsSince(since));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getLogCount() {
        return ResponseEntity.ok(Map.of("count", logStorageService.getLogCount()));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> clearLogs() {
        logStorageService.clearLogs();
        return ResponseEntity.ok(Map.of("message", "Logs cleared"));
    }
}
