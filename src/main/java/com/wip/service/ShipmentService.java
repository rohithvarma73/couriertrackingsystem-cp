package com.wip.service;

import com.wip.dto.ShipmentDto;
import java.util.List;

public interface ShipmentService {
    ShipmentDto addShipment(Long parcelId);
    List<ShipmentDto> getAllShipments();
    ShipmentDto getShipmentById(Long id);
    ShipmentDto getShipmentByTrackingNumber(String trackingNumber);
    ShipmentDto updateShipmentLocation(Long id, String currentLocation);
    ShipmentDto updateShipment(Long id, ShipmentDto shipmentDto);
    void deleteShipment(Long id);
}