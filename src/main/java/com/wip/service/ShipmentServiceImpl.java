package com.wip.service;

import com.wip.dto.ShipmentDto;
import com.wip.entity.Parcel;
import com.wip.entity.Shipment;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.ParcelRepository;
import com.wip.repository.ShipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ParcelRepository parcelRepository;

    public ShipmentServiceImpl(ShipmentRepository shipmentRepository, ParcelRepository parcelRepository) {
        this.shipmentRepository = shipmentRepository;
        this.parcelRepository = parcelRepository;
    }

    @Override
    public ShipmentDto addShipment(ShipmentDto shipmentDto) {
        Shipment shipment = toEntity(shipmentDto);
        Parcel parcel = parcelRepository.findById(shipmentDto.getParcelId())
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + shipmentDto.getParcelId()));
        shipment.setParcel(parcel);
        return toDto(shipmentRepository.save(shipment));
    }

    @Override
    public List<ShipmentDto> getAllShipments() {
        return shipmentRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ShipmentDto getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
        return toDto(shipment);
    }

    @Override
    public ShipmentDto updateShipment(Long id, ShipmentDto shipmentDto) {
        Shipment existing = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));

        existing.setTrackingNumber(shipmentDto.getTrackingNumber());
        existing.setShipmentDate(shipmentDto.getShipmentDate());
        existing.setCurrentLocation(shipmentDto.getCurrentLocation());
        existing.setEstimatedDeliveryDate(shipmentDto.getEstimatedDeliveryDate());

        if (shipmentDto.getParcelId() != null) {
            Parcel parcel = parcelRepository.findById(shipmentDto.getParcelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + shipmentDto.getParcelId()));
            existing.setParcel(parcel);
        }

        return toDto(shipmentRepository.save(existing));
    }

    @Override
    public void deleteShipment(Long id) {
        Shipment existing = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
        shipmentRepository.delete(existing);
    }

    private ShipmentDto toDto(Shipment shipment) {
        ShipmentDto dto = new ShipmentDto();
        dto.setShipmentId(shipment.getShipmentId());
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setShipmentDate(shipment.getShipmentDate());
        dto.setCurrentLocation(shipment.getCurrentLocation());
        dto.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
        if (shipment.getParcel() != null) {
            dto.setParcelId(shipment.getParcel().getParcelId());
        }
        return dto;
    }

    private Shipment toEntity(ShipmentDto dto) {
        Shipment shipment = new Shipment();
        shipment.setShipmentId(dto.getShipmentId());
        shipment.setTrackingNumber(dto.getTrackingNumber());
        shipment.setShipmentDate(dto.getShipmentDate());
        shipment.setCurrentLocation(dto.getCurrentLocation());
        shipment.setEstimatedDeliveryDate(dto.getEstimatedDeliveryDate());
        return shipment;
    }
}