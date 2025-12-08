package com.hansal.verrechnungsprogramm.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validierungsfehler");

        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> {
                    Map<String, String> errorDetail = new HashMap<>();
                    errorDetail.put("field", error.getField());
                    errorDetail.put("message", translateFieldError(error));
                    return errorDetail;
                })
                .collect(Collectors.toList());

        response.put("errors", errors);
        response.put("message", "Bitte überprüfen Sie Ihre Eingaben");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validierungsfehler");

        List<Map<String, String>> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    Map<String, String> errorDetail = new HashMap<>();
                    errorDetail.put("field", getFieldName(violation));
                    errorDetail.put("message", translateConstraintViolation(violation));
                    return errorDetail;
                })
                .collect(Collectors.toList());

        response.put("errors", errors);
        response.put("message", "Bitte überprüfen Sie Ihre Eingaben");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Serverfehler");
        response.put("message", translateRuntimeException(ex.getMessage()));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Unerwarteter Fehler");
        response.put("message", "Ein unerwarteter Fehler ist aufgetreten. Bitte versuchen Sie es erneut.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private String getFieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        // Extract the last part of the path (e.g., "items[0].weight" -> "weight")
        if (path.contains(".")) {
            return path.substring(path.lastIndexOf('.') + 1);
        }
        return path;
    }

    private String translateFieldError(FieldError error) {
        String field = error.getField();
        String defaultMessage = error.getDefaultMessage();

        // Translate common field names and messages to German
        switch (field) {
            case "customerName":
                return "Kundenname ist erforderlich";
            case "weight":
                return "Gewicht ist erforderlich und muss positiv sein";
            case "unitPrice":
                return "Preis ist erforderlich";
            case "items":
                return "Mindestens ein Artikel ist erforderlich";
            default:
                return defaultMessage != null ? defaultMessage : "Ungültiger Wert";
        }
    }

    private String translateConstraintViolation(ConstraintViolation<?> violation) {
        String field = getFieldName(violation);
        String message = violation.getMessage();

        // Translate constraint violation messages to German
        if (message.contains("Weight is required")) {
            return "Gewicht ist erforderlich";
        }
        if (message.contains("Unit price is required")) {
            return "Preis ist erforderlich";
        }
        if (message.contains("Customer name is required")) {
            return "Kundenname ist erforderlich";
        }
        if (message.contains("must be positive") || message.contains("must be greater than")) {
            return field + " muss einen positiven Wert haben";
        }
        if (message.contains("must not be null") || message.contains("is required")) {
            return field + " ist erforderlich";
        }

        return message;
    }

    private String translateRuntimeException(String message) {
        if (message == null) {
            return "Ein Fehler ist aufgetreten";
        }
        if (message.contains("not found")) {
            return "Der angeforderte Eintrag wurde nicht gefunden";
        }
        if (message.contains("Order not found")) {
            return "Bestellung wurde nicht gefunden";
        }
        if (message.contains("Product not found")) {
            return "Produkt wurde nicht gefunden";
        }
        if (message.contains("Invoice not found")) {
            return "Rechnung wurde nicht gefunden";
        }
        return message;
    }
}
