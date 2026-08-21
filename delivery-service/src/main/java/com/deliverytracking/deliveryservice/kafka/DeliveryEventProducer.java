package com.deliverytracking.deliveryservice.kafka;

import com.deliverytracking.deliveryservice.event.DeliveryAssignedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeliveryEventProducer {

    private static final String ASSIGNED_TOPIC = "delivery.assigned";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeliveryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDeliveryAssigned(DeliveryAssignedEvent event) {
        kafkaTemplate.send(ASSIGNED_TOPIC, event.shipmentId().toString(), event);
    }
}
