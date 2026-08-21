package com.deliverytracking.deliveryservice.service;

import com.deliverytracking.deliveryservice.client.UserDto;
import com.deliverytracking.deliveryservice.client.UserServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AgentValidationService {

    private static final Logger log = LoggerFactory.getLogger(AgentValidationService.class);

    private final UserServiceClient userServiceClient;

    public AgentValidationService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetAgent")
    public UserDto validateAgent(Long agentId) {
        return userServiceClient.getUserById(agentId);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackGetAvailableAgents")
    public List<UserDto> findAvailableAgents() {
        return userServiceClient.getAvailableAgents();
    }

    private UserDto fallbackGetAgent(Long agentId, Throwable throwable) {
        log.error("Fallback triggered for agentId={}. Exception type: {}, message: {}",
                agentId, throwable.getClass().getName(), throwable.getMessage(), throwable);
        throw new DeliveryAssignmentException(
                "Unable to validate agent " + agentId + " because user-service is unavailable", throwable);
    }

    private List<UserDto> fallbackGetAvailableAgents(Throwable throwable) {
        log.error("Fallback triggered while finding available agents. Exception type: {}, message: {}",
                throwable.getClass().getName(), throwable.getMessage(), throwable);
        throw new DeliveryAssignmentException("Unable to find available agents because user-service is unavailable", throwable);
    }
}
