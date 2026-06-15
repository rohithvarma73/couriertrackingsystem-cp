package com.wip.service;

import com.wip.dto.ShipmentDto;
import com.wip.entity.Parcel;
import com.wip.entity.Shipment;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.ParcelRepository;
import com.wip.repository.ShipmentRepository;
import com.wip.util.TrackingNumberGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    public ShipmentDto addShipment(Long parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found with id: " + parcelId));

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(TrackingNumberGenerator.generateTrackingNumber());
        shipment.setShipmentDate(LocalDate.now());
        shipment.setCurrentLocation(parcel.getSourceAddress());
        shipment.setEstimatedDeliveryDate(LocalDate.now().plusDays(3));
        shipment.setParcel(parcel);

        return toDto(shipmentRepository.save(shipment));
    }

    @Override
    public List<ShipmentDto> getAllShipments() {
        return shipmentRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ShipmentDto getShipmentById(Long id) {
        return toDto(shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id)));
    }

    @Override
    public ShipmentDto getShipmentByTrackingNumber(String trackingNumber) {
        return toDto(shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with tracking number: " + trackingNumber)));
    }

    @Override
    public ShipmentDto updateShipmentLocation(Long id, String currentLocation) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
        shipment.setCurrentLocation(currentLocation);
        return toDto(shipmentRepository.save(shipment));
    }

    @Override
    public ShipmentDto updateShipment(Long id, ShipmentDto shipmentDto) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));

        shipment.setTrackingNumber(shipmentDto.getTrackingNumber());
        shipment.setShipmentDate(shipmentDto.getShipmentDate());
        shipment.setCurrentLocation(shipmentDto.getCurrentLocation());
        shipment.setEstimatedDeliveryDate(shipmentDto.getEstimatedDeliveryDate());

        return toDto(shipmentRepository.save(shipment));
    }

    @Override
    public void deleteShipment(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
        shipmentRepository.delete(shipment);
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
}