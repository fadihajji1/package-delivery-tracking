package com.deliverytracking.shipmentservice.kafka;

import com.deliverytracking.shipmentservice.event.ShipmentCreatedEvent;
import com.deliverytracking.shipmentservice.event.ShipmentStatusChangedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ShipmentEventProducer {

    private static final String CREATED_TOPIC = "shipment.created";
    private static final String STATUS_CHANGED_TOPIC = "shipment.status-changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ShipmentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishShipmentCreated(ShipmentCreatedEvent event) {
        kafkaTemplate.send(CREATED_TOPIC, event.shipmentId().toString(), event);
    }

    public void publishStatusChanged(ShipmentStatusChangedEvent event) {
        kafkaTemplate.send(STATUS_CHANGED_TOPIC, event.shipmentId().toString(), event);
    }
}