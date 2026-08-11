package com.deliverytracking.shipmentservice.exception;

import com.deliverytracking.shipmentservice.service.CustomerValidationException;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<String> handleCustomerNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
    }

    @ExceptionHandler(CustomerValidationException.class)
    public ResponseEntity<String> handleServiceUnavailable(CustomerValidationException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }
}