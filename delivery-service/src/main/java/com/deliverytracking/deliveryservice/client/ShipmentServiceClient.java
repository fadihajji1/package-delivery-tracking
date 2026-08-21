package com.deliverytracking.deliveryservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "shipment-service")
public interface ShipmentServiceClient {

    @PatchMapping("/shipments/{id}/status")
    ShipmentDto updateShipmentStatus(@PathVariable("id") Long shipmentId,
                                      @RequestBody UpdateShipmentStatusRequest request);
}
