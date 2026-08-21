package com.deliverytracking.deliveryservice.exception;

import com.deliverytracking.deliveryservice.service.DeliveryAssignmentException;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<String> handleNotFound(FeignException.NotFound exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Referenced resource not found");
    }

    @ExceptionHandler(DeliveryAssignmentException.class)
    public ResponseEntity<String> handleServiceUnavailable(DeliveryAssignmentException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(exception.getMessage());
    }
}
