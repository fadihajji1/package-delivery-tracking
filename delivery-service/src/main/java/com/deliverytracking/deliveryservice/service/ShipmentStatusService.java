package com.deliverytracking.deliveryservice.service;

import com.deliverytracking.deliveryservice.client.ShipmentServiceClient;
import com.deliverytracking.deliveryservice.client.ShipmentDto;
import com.deliverytracking.deliveryservice.client.UpdateShipmentStatusRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ShipmentStatusService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentStatusService.class);

    private final ShipmentServiceClient shipmentServiceClient;

    public ShipmentStatusService(ShipmentServiceClient shipmentServiceClient) {
        this.shipmentServiceClient = shipmentServiceClient;
    }

    @CircuitBreaker(name = "shipmentService", fallbackMethod = "fallbackUpdateStatus")
    public ShipmentDto markOutForDelivery(Long shipmentId) {
        return updateStatus(shipmentId, "OUT_FOR_DELIVERY");
    }

    public ShipmentDto updateStatus(Long shipmentId, String status) {
        return shipmentServiceClient.updateShipmentStatus(
                shipmentId, new UpdateShipmentStatusRequest(status));
    }

    private void fallbackUpdateStatus(Long shipmentId, Throwable throwable) {
        log.error("Fallback triggered for shipmentId={}. Exception type: {}, message: {}",
                shipmentId, throwable.getClass().getName(), throwable.getMessage(), throwable);
        throw new DeliveryAssignmentException(
                "Unable to update shipment " + shipmentId + " because shipment-service is unavailable", throwable);
    }
}
