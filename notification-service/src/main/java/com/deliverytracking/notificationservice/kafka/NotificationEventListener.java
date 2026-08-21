package com.deliverytracking.notificationservice.kafka;

import com.deliverytracking.notificationservice.model.Notification;
import com.deliverytracking.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationRepository notificationRepository;

    public NotificationEventListener(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(
            topics = "shipment.created",
            groupId = "notification-service-group",
            properties = "spring.json.value.default.type=com.deliverytracking.notificationservice.kafka.ShipmentCreatedEvent")
    public void handleShipmentCreated(ShipmentCreatedEvent event) {
        saveNotification(event.customerId(), event.shipmentId(), "shipment.created",
                "Shipment created from " + event.origin() + " to " + event.destination());
    }

    @KafkaListener(
            topics = "shipment.status-changed",
            groupId = "notification-service-group",
            properties = "spring.json.value.default.type=com.deliverytracking.notificationservice.kafka.ShipmentStatusChangedEvent")
    public void handleShipmentStatusChanged(ShipmentStatusChangedEvent event) {
        saveNotification(event.customerId(), event.shipmentId(), "shipment.status-changed",
                "Shipment status changed from " + event.oldStatus() + " to " + event.newStatus());
    }

    @KafkaListener(
            topics = "delivery.assigned",
            groupId = "notification-service-group",
            properties = "spring.json.value.default.type=com.deliverytracking.notificationservice.kafka.DeliveryAssignedEvent")
    public void handleDeliveryAssigned(DeliveryAssignedEvent event) {
        saveNotification(event.customerId(), event.shipmentId(), "delivery.assigned",
                "Delivery assigned to agent " + event.agentId());
    }

    private void saveNotification(Long userId, Long shipmentId, String eventType, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setShipmentId(shipmentId);
        notification.setEventType(eventType);
        notification.setMessage(message);
        notificationRepository.save(notification);
        log.info("Simulated notification for shipmentId={}, eventType={}, message={}",
                shipmentId, eventType, message);
    }
}
