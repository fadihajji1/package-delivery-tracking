package com.deliverytracking.trackingservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "tracking_records")
@Data
@NoArgsConstructor
public class TrackingRecord {

    @Id
    private String id;

    private Long shipmentId;
    private Long customerId;
    private String currentStatus;
    private List<TrackingEvent> events = new ArrayList<>();

    public void addEvent(TrackingEvent event) {
        this.events.add(event);
        this.currentStatus = event.getStatus();
    }

    @Data
    @NoArgsConstructor
    public static class TrackingEvent {
        private String status;
        private String description;
        private LocalDateTime timestamp;

        public TrackingEvent(String status, String description, LocalDateTime timestamp) {
            this.status = status;
            this.description = description;
            this.timestamp = timestamp;
        }
    }
}