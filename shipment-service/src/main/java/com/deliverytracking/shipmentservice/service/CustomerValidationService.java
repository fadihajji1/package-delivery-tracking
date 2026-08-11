package com.deliverytracking.shipmentservice.service;

import com.deliverytracking.shipmentservice.client.UserDto;
import com.deliverytracking.shipmentservice.client.UserServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CustomerValidationService {

    private static final Logger log = LoggerFactory.getLogger(CustomerValidationService.class);

    private final UserServiceClient userServiceClient;

    public CustomerValidationService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetUser")
    public UserDto validateCustomer(Long customerId) {
        return userServiceClient.getUserById(customerId);
    }

    private UserDto fallbackGetUser(Long customerId, Throwable throwable) {
        log.error("Fallback triggered for customerId={}. Exception type: {}, message: {}",
                customerId, throwable.getClass().getName(), throwable.getMessage(), throwable);
        throw new CustomerValidationException(
                "Unable to validate customer " + customerId + " — user-service is unavailable", throwable);
    }
}