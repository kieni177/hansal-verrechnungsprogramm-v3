package com.hansal.verrechnungsprogramm.service;

import com.hansal.verrechnungsprogramm.dto.LogEntryDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service
public class LogStorageService {

    private static final int MAX_LOG_ENTRIES = 1000;
    private final LinkedList<LogEntryDTO> logEntries = new LinkedList<>();

    public synchronized void addLogEntry(String level, String logger, String message, String thread) {
        LogEntryDTO entry = new LogEntryDTO(
                LocalDateTime.now(),
                level,
                logger,
                message,
                thread
        );

        logEntries.addFirst(entry);

        // Keep only the last MAX_LOG_ENTRIES
        while (logEntries.size() > MAX_LOG_ENTRIES) {
            logEntries.removeLast();
        }
    }

    public synchronized List<LogEntryDTO> getRecentLogs(int limit) {
        int count = Math.min(limit, logEntries.size());
        return new ArrayList<>(logEntries.subList(0, count));
    }

    public synchronized List<LogEntryDTO> getLogsSince(LocalDateTime since) {
        List<LogEntryDTO> result = new ArrayList<>();
        for (LogEntryDTO entry : logEntries) {
            if (entry.getTimestamp().isAfter(since)) {
                result.add(entry);
            } else {
                break; // Logs are ordered newest first
            }
        }
        return result;
    }

    public synchronized List<LogEntryDTO> getLogsByLevel(String level, int limit) {
        List<LogEntryDTO> result = new ArrayList<>();
        for (LogEntryDTO entry : logEntries) {
            if (entry.getLevel().equalsIgnoreCase(level)) {
                result.add(entry);
                if (result.size() >= limit) {
                    break;
                }
            }
        }
        return result;
    }

    public synchronized void clearLogs() {
        logEntries.clear();
    }

    public synchronized int getLogCount() {
        return logEntries.size();
    }
}
