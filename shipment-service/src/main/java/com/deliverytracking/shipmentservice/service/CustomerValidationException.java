package com.deliverytracking.shipmentservice.service;

public class CustomerValidationException extends RuntimeException {
    public CustomerValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}