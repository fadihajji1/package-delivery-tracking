package com.deliverytracking.shipmentservice.config;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public Customizer<CircuitBreakerRegistry> customizeCircuitBreaker() {
        return registry -> {
            CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                    .slidingWindowSize(10)
                    .minimumNumberOfCalls(5)
                    .permittedNumberOfCallsInHalfOpenState(3)
                    .waitDurationInOpenState(Duration.ofSeconds(10))
                    .failureRateThreshold(50)
                    .ignoreExceptions(FeignException.NotFound.class)
                    .build();
            registry.addConfiguration("userService", config);
        };
    }
}