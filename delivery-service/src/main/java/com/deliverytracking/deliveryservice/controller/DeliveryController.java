package com.deliverytracking.deliveryservice.controller;

import com.deliverytracking.deliveryservice.dto.CreateDeliveryRequest;
import com.deliverytracking.deliveryservice.dto.DeliveryResponse;
import com.deliverytracking.deliveryservice.dto.UpdateDeliveryStatusRequest;
import com.deliverytracking.deliveryservice.model.Delivery;
import com.deliverytracking.deliveryservice.repository.DeliveryRepository;
import com.deliverytracking.deliveryservice.service.DeliveryAssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    private final DeliveryAssignmentService assignmentService;
    private final DeliveryRepository deliveryRepository;

    public DeliveryController(DeliveryAssignmentService assignmentService,
                               DeliveryRepository deliveryRepository) {
        this.assignmentService = assignmentService;
        this.deliveryRepository = deliveryRepository;
    }

    @PostMapping
    public ResponseEntity<DeliveryResponse> assignDelivery(
            @Valid @RequestBody CreateDeliveryRequest request) {
        Delivery delivery = assignmentService.assignDelivery(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(delivery));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDelivery(@PathVariable Long id) {
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Delivery not found"));
        return ResponseEntity.ok(toResponse(delivery));
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        return ResponseEntity.ok(toResponse(assignmentService.updateStatus(id, request.status())));
    }

    private DeliveryResponse toResponse(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(), delivery.getShipmentId(), delivery.getAgentId(),
                delivery.getAssignedAt(), delivery.getStatus());
    }
}
