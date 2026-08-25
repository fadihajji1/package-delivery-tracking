package com.deliverytracking.deliveryservice.service;

import com.deliverytracking.deliveryservice.client.ShipmentDto;
import com.deliverytracking.deliveryservice.client.UserDto;
import com.deliverytracking.deliveryservice.dto.CreateDeliveryRequest;
import com.deliverytracking.deliveryservice.event.DeliveryAssignedEvent;
import com.deliverytracking.deliveryservice.kafka.DeliveryEventProducer;
import com.deliverytracking.deliveryservice.model.Delivery;
import com.deliverytracking.deliveryservice.model.DeliveryStatus;
import com.deliverytracking.deliveryservice.repository.DeliveryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class DeliveryAssignmentServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private AgentValidationService agentValidationService;

    @Mock
    private ShipmentStatusService shipmentStatusService;

    @Mock
    private DeliveryEventProducer eventProducer;

    @InjectMocks
    private DeliveryAssignmentService assignmentService;

    @Test
    void assignDeliveryUsesExplicitAgentAndPublishesCustomerAwareEvent() {
        UserDto agent = new UserDto(2L, "Agent", "agent@example.com", "AGENT", true, null);
        ShipmentDto shipment = new ShipmentDto(1L, 4L, "OUT_FOR_DELIVERY", "Tunis", "Sfax", null, null);
        when(agentValidationService.validateAgent(2L)).thenReturn(agent);
        Delivery savedDelivery = new Delivery();
        savedDelivery.setId(1L);
        savedDelivery.setShipmentId(1L);
        savedDelivery.setAgentId(2L);
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(savedDelivery);
        when(shipmentStatusService.markOutForDelivery(1L)).thenReturn(shipment);

        Delivery delivery = assignmentService.assignDelivery(new CreateDeliveryRequest(1L, 2L));

        assertEquals(1L, delivery.getShipmentId());
        assertEquals(2L, delivery.getAgentId());
        verify(agentValidationService).validateAgent(2L);
        verify(shipmentStatusService).markOutForDelivery(1L);

        ArgumentCaptor<DeliveryAssignedEvent> eventCaptor = ArgumentCaptor.forClass(DeliveryAssignedEvent.class);
        verify(eventProducer).publishDeliveryAssigned(eventCaptor.capture());
        assertEquals(4L, eventCaptor.getValue().customerId());
    }

    @Test
    void assignDeliverySelectsFirstAvailableAgentWhenAgentIdIsMissing() {
        UserDto agent = new UserDto(7L, "Available Agent", "agent@example.com", "AGENT", true, null);
        ShipmentDto shipment = new ShipmentDto(3L, 9L, "OUT_FOR_DELIVERY", "Tunis", "Sfax", null, null);
        when(agentValidationService.findAvailableAgents()).thenReturn(List.of(agent));
        Delivery savedDelivery = new Delivery();
        savedDelivery.setId(1L);
        savedDelivery.setShipmentId(3L);
        savedDelivery.setAgentId(7L);
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(savedDelivery);
        when(shipmentStatusService.markOutForDelivery(3L)).thenReturn(shipment);

        Delivery delivery = assignmentService.assignDelivery(new CreateDeliveryRequest(3L, null));

        assertEquals(7L, delivery.getAgentId());
        verify(agentValidationService).findAvailableAgents();
    }

    @Test
    void assignDeliveryFailsWhenNoAgentIsAvailable() {
        when(agentValidationService.findAvailableAgents()).thenReturn(List.of());

        DeliveryAssignmentException exception = assertThrows(
                DeliveryAssignmentException.class,
                () -> assignmentService.assignDelivery(new CreateDeliveryRequest(1L, null)));

        assertEquals("No available delivery agents", exception.getMessage());
    }

    @Test
    void updateStatusMapsCompletedDeliveryToDeliveredShipment() {
        Delivery delivery = new Delivery();
        delivery.setShipmentId(8L);
        delivery.setAgentId(2L);
        when(deliveryRepository.findById(5L)).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(delivery)).thenReturn(delivery);

        Delivery result = assignmentService.updateStatus(5L, DeliveryStatus.COMPLETED);

        assertEquals(DeliveryStatus.COMPLETED, result.getStatus());
        verify(shipmentStatusService).updateStatus(8L, "DELIVERED");
    }
}
