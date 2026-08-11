package com.deliverytracking.shipmentservice.controller;

import com.deliverytracking.shipmentservice.client.UserDto;
import com.deliverytracking.shipmentservice.dto.CreateShipmentRequest;
import com.deliverytracking.shipmentservice.dto.ShipmentResponse;
import com.deliverytracking.shipmentservice.event.ShipmentCreatedEvent;
import com.deliverytracking.shipmentservice.kafka.ShipmentEventProducer;
import com.deliverytracking.shipmentservice.model.Shipment;
import com.deliverytracking.shipmentservice.repository.ShipmentRepository;
import com.deliverytracking.shipmentservice.service.CustomerValidationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentRepository shipmentRepository;
    private final CustomerValidationService customerValidationService;
    private final ShipmentEventProducer eventProducer;

    public ShipmentController(ShipmentRepository shipmentRepository,
                              CustomerValidationService customerValidationService,
                              ShipmentEventProducer eventProducer) {
        this.shipmentRepository = shipmentRepository;
        this.customerValidationService = customerValidationService;
        this.eventProducer = eventProducer;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        UserDto customer = customerValidationService.validateCustomer(request.customerId());

        Shipment shipment = new Shipment();
        shipment.setCustomerId(request.customerId());
        shipment.setOrigin(request.origin());
        shipment.setDestination(request.destination());

        Shipment saved = shipmentRepository.save(shipment);

        eventProducer.publishShipmentCreated(new ShipmentCreatedEvent(
                saved.getId(), saved.getCustomerId(), saved.getOrigin(),
                saved.getDestination(), saved.getCreatedAt()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found"));
        return ResponseEntity.ok(toResponse(shipment));
    }

    private ShipmentResponse toResponse(Shipment s) {
        return new ShipmentResponse(s.getId(), s.getCustomerId(), s.getStatus(),
                s.getOrigin(), s.getDestination(), s.getCreatedAt(), s.getUpdatedAt());
    }
}