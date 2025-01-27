package com.example.battery_api.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Validation failed for one or more fields.");

        List<Map<String, String>> errors = new ArrayList<>();

        // Extract errors with field and index
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField(); // Field name
            String message = error.getDefaultMessage(); // Validation message

            // Extract index from field name (e.g., requestDTOs[1].name)
            String objectName = error.getObjectName();
            int index = extractIndexFromFieldPath(objectName + "." + field);

            // Add to errors list with index
            Map<String, String> errorDetails = new HashMap<>();
            // Only add index if it is not -1
            if (index != -1) {
                errorDetails.put("index", String.valueOf(index));
            }
            errorDetails.put("field", field);
            errorDetails.put("message", message);
            errors.add(errorDetails);
        });

        response.put("errors", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private int extractIndexFromFieldPath(String fieldPath) {
        try {
            // Use regex to extract the index from the field path (e.g., requestDTOs[1].name -> 1)
            Pattern pattern = Pattern.compile("\\[(\\d+)]");
            Matcher matcher = pattern.matcher(fieldPath);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // Default to -1 if no index is found
        }
        return -1;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Validation failed for one or more fields.");

        List<Map<String, String>> errors = new ArrayList<>();

        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            int index = extractIndexFromFieldPath(propertyPath);
            System.out.println(index);
            Map<String, String> errorDetails = new HashMap<>();
            // Only add index if it is not -1
            if (index != -1) {
                errorDetails.put("index", String.valueOf(index));
            }
            errorDetails.put("field", propertyPath.substring(propertyPath.lastIndexOf('.') + 1));
            errorDetails.put("message", violation.getMessage());
            errors.add(errorDetails);
        });

        response.put("errors", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
