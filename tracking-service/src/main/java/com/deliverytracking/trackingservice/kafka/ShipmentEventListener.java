package com.deliverytracking.trackingservice.kafka;

import com.deliverytracking.trackingservice.model.TrackingRecord;
import com.deliverytracking.trackingservice.repository.TrackingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ShipmentEventListener {

    private static final Logger log = LoggerFactory.getLogger(ShipmentEventListener.class);

    private final TrackingRepository trackingRepository;

    public ShipmentEventListener(TrackingRepository trackingRepository) {
        this.trackingRepository = trackingRepository;
    }

    @KafkaListener(topics = "shipment.created", groupId = "tracking-service-group")
    public void handleShipmentCreated(ShipmentCreatedEvent event) {
        log.info("Received shipment.created event: {}", event);

        TrackingRecord record = trackingRepository.findByShipmentId(event.shipmentId())
                .orElseGet(TrackingRecord::new);

        record.setShipmentId(event.shipmentId());
        record.setCustomerId(event.customerId());
        record.addEvent(new TrackingRecord.TrackingEvent(
                "CREATED",
                "Shipment created from " + event.origin() + " to " + event.destination(),
                event.createdAt()
        ));

        trackingRepository.save(record);
        log.info("Saved tracking record for shipmentId={}", event.shipmentId());
    }

        @KafkaListener(
            topics = "shipment.status-changed",
            groupId = "tracking-service-group",
            properties = {
                "spring.json.value.default.type=com.deliverytracking.trackingservice.kafka.ShipmentStatusChangedEvent",
                "spring.json.use.type.headers=false"
            })
        public void handleShipmentStatusChanged(ShipmentStatusChangedEvent event) {
        log.info("Received shipment.status-changed event: {}", event);

        TrackingRecord record = trackingRepository.findByShipmentId(event.shipmentId())
            .orElseGet(TrackingRecord::new);

        record.setShipmentId(event.shipmentId());
        record.setCustomerId(event.customerId());
        record.addEvent(new TrackingRecord.TrackingEvent(
            event.newStatus(),
            "Shipment status changed from " + event.oldStatus() + " to " + event.newStatus(),
            event.changedAt()
        ));

        trackingRepository.save(record);
        log.info("Saved status change for shipmentId={}", event.shipmentId());
        }

    @KafkaListener(
            topics = "delivery.assigned",
            groupId = "tracking-service-group",
            properties = {
                "spring.json.value.default.type=com.deliverytracking.trackingservice.kafka.DeliveryAssignedEvent",
                "spring.json.use.type.headers=false"
            })
    public void handleDeliveryAssigned(DeliveryAssignedEvent event) {
        log.info("Received delivery.assigned event: {}", event);

        TrackingRecord record = trackingRepository.findByShipmentId(event.shipmentId())
            .orElseGet(TrackingRecord::new);

        record.setShipmentId(event.shipmentId());
        record.setCustomerId(event.customerId());
        record.addEvent(new TrackingRecord.TrackingEvent(
            "OUT_FOR_DELIVERY",
            "Delivery assigned to agent " + event.agentId(),
            event.assignedAt()
        ));

        trackingRepository.save(record);
        log.info("Saved delivery assignment for shipmentId={}", event.shipmentId());
        }
}