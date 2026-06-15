package com.wip.service;

import com.wip.dto.ShipmentDto;
import java.util.List;

public interface ShipmentService {
    ShipmentDto addShipment(ShipmentDto shipmentDto);
    List<ShipmentDto> getAllShipments();
    ShipmentDto getShipmentById(Long id);
    ShipmentDto updateShipment(Long id, ShipmentDto shipmentDto);
    void deleteShipment(Long id);
}