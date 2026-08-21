package com.deliverytracking.deliveryservice.service;

import com.deliverytracking.deliveryservice.client.UserDto;
import com.deliverytracking.deliveryservice.client.ShipmentDto;
import com.deliverytracking.deliveryservice.dto.CreateDeliveryRequest;
import com.deliverytracking.deliveryservice.event.DeliveryAssignedEvent;
import com.deliverytracking.deliveryservice.kafka.DeliveryEventProducer;
import com.deliverytracking.deliveryservice.model.Delivery;
import com.deliverytracking.deliveryservice.model.DeliveryStatus;
import com.deliverytracking.deliveryservice.repository.DeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryAssignmentService {

    private final DeliveryRepository deliveryRepository;
    private final AgentValidationService agentValidationService;
    private final ShipmentStatusService shipmentStatusService;
    private final DeliveryEventProducer eventProducer;

    public DeliveryAssignmentService(DeliveryRepository deliveryRepository,
                                      AgentValidationService agentValidationService,
                                      ShipmentStatusService shipmentStatusService,
                                      DeliveryEventProducer eventProducer) {
        this.deliveryRepository = deliveryRepository;
        this.agentValidationService = agentValidationService;
        this.shipmentStatusService = shipmentStatusService;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public Delivery assignDelivery(CreateDeliveryRequest request) {
        UserDto agent = selectAgent(request.agentId());

        Delivery delivery = new Delivery();
        delivery.setShipmentId(request.shipmentId());
        delivery.setAgentId(agent.id());
        Delivery saved = deliveryRepository.save(delivery);

        ShipmentDto shipment = shipmentStatusService.markOutForDelivery(saved.getShipmentId());
        eventProducer.publishDeliveryAssigned(new DeliveryAssignedEvent(
            saved.getShipmentId(), shipment.customerId(), saved.getAgentId(), saved.getAssignedAt()));

        return saved;
    }

    @Transactional
    public Delivery updateStatus(Long deliveryId, DeliveryStatus status) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryAssignmentException("Delivery not found", null));
        delivery.setStatus(status);
        Delivery saved = deliveryRepository.save(delivery);
        shipmentStatusService.updateStatus(saved.getShipmentId(), toShipmentStatus(status));
        return saved;
    }

    private String toShipmentStatus(DeliveryStatus status) {
        return switch (status) {
            case ASSIGNED -> "OUT_FOR_DELIVERY";
            case IN_PROGRESS -> "IN_TRANSIT";
            case COMPLETED -> "DELIVERED";
            case FAILED -> "FAILED";
        };
    }

    private UserDto selectAgent(Long agentId) {
        if (agentId != null) {
            return agentValidationService.validateAgent(agentId);
        }
        return agentValidationService.findAvailableAgents().stream()
                .findFirst()
                .orElseThrow(() -> new DeliveryAssignmentException("No available delivery agents", null));
    }
}
