package com.wip.service;

import com.wip.dto.ShipmentDto;
import java.util.List;

/**
 * ShipmentService Component.
 * 
 * Handles operations and data related to ShipmentService.
 */
public interface ShipmentService {
    ShipmentDto addShipment(Long parcelId);
    List<ShipmentDto> getAllShipments();
    ShipmentDto getShipmentById(Long id);
    ShipmentDto getShipmentByTrackingNumber(String trackingNumber);
    ShipmentDto updateShipmentLocation(Long id, String currentLocation);
    ShipmentDto updateShipment(Long id, ShipmentDto shipmentDto);
    ShipmentDto getShipmentByParcelId(Long parcelId);
    void deleteShipment(Long id);
    List<ShipmentDto> search(String keyword);
}
