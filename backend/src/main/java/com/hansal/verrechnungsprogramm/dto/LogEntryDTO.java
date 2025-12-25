package com.hansal.verrechnungsprogramm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntryDTO {
    private LocalDateTime timestamp;
    private String level;
    private String logger;
    private String message;
    private String thread;
}
