package com.wip.service;

import com.wip.dto.ShipmentDto;
import com.wip.entity.Parcel;
import com.wip.entity.Shipment;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.ParcelRepository;
import com.wip.repository.ShipmentRepository;
import com.wip.repository.TrackingUpdateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ParcelRepository parcelRepository;
    private final TrackingUpdateRepository trackingUpdateRepository;

    public ShipmentServiceImpl(ShipmentRepository shipmentRepository,
                               ParcelRepository parcelRepository,
                               TrackingUpdateRepository trackingUpdateRepository) {
        this.shipmentRepository = shipmentRepository;
        this.parcelRepository = parcelRepository;
        this.trackingUpdateRepository = trackingUpdateRepository;
    }

    @Override
    public ShipmentDto addShipment(Long parcelId) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new ResourceNotFoundException("Parcel not found"));


        Optional<Shipment> existingShipment = shipmentRepository.findByParcel_ParcelId(parcelId);
        if (existingShipment.isPresent()) {
            throw new IllegalStateException("Shipment already exists for this parcel. View the existing shipment instead.");
        }
        

        Shipment shipment = new Shipment();
        shipment.setParcel(parcel);
        shipment.setTrackingNumber("TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        shipment.setShipmentDate(LocalDate.now());
        shipment.setCurrentLocation(parcel.getSourceAddress());
        shipment.setEstimatedDeliveryDate(LocalDate.now().plusDays(3));

        return toDto(shipmentRepository.save(shipment));
    }

    @Override
    public List<ShipmentDto> getAllShipments() {
        return shipmentRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ShipmentDto getShipmentById(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
        return toDto(shipment);
    }

    @Override
    public ShipmentDto getShipmentByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
        return toDto(shipment);
    }

    @Override
    public ShipmentDto updateShipmentLocation(Long id, String currentLocation) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));
        shipment.setCurrentLocation(currentLocation);
        return toDto(shipmentRepository.save(shipment));
    }

    @Override
    public ShipmentDto updateShipment(Long id, ShipmentDto shipmentDto) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        shipment.setCurrentLocation(shipmentDto.getCurrentLocation());
        shipment.setEstimatedDeliveryDate(shipmentDto.getEstimatedDeliveryDate());

        if (shipmentDto.getShipmentDate() != null) {
            shipment.setShipmentDate(shipmentDto.getShipmentDate());
        }

        return toDto(shipmentRepository.save(shipment));
    }

    @Transactional
    @Override
    public void deleteShipment(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        trackingUpdateRepository.deleteByShipment_ShipmentId(id);
        shipmentRepository.delete(shipment);
    }

    private ShipmentDto toDto(Shipment shipment) {
        ShipmentDto dto = new ShipmentDto();
        dto.setShipmentId(shipment.getShipmentId());
        dto.setParcelId(shipment.getParcel() != null ? shipment.getParcel().getParcelId() : null);
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setShipmentDate(shipment.getShipmentDate());
        dto.setCurrentLocation(shipment.getCurrentLocation());
        dto.setEstimatedDeliveryDate(shipment.getEstimatedDeliveryDate());
        dto.setCustomerName(
                shipment.getParcel() != null && shipment.getParcel().getCustomer() != null
                        ? shipment.getParcel().getCustomer().getCustomerName()
                        : null
        );
        dto.setReceiverPhone(shipment.getParcel() != null ? shipment.getParcel().getReceiverPhone() : null);
        return dto;
    }
    @Override
    public ShipmentDto getShipmentByParcelId(Long parcelId) {
        return shipmentRepository.findByParcel_ParcelId(parcelId)
                .map(this::toDto)
                .orElse(null);
    }
    @Override
    public List<ShipmentDto> search(String keyword) {
        List<ShipmentDto> shipments = getAllShipments();

        if (keyword == null || keyword.isBlank()) {
            return shipments;
        }

        String k = keyword.toLowerCase();

        return shipments.stream()
                .filter(s ->
                        (s.getShipmentId() != null && String.valueOf(s.getShipmentId()).contains(k)) ||
                        (s.getParcelId() != null && String.valueOf(s.getParcelId()).contains(k)) ||
                        (s.getTrackingNumber() != null && s.getTrackingNumber().toLowerCase().contains(k)) ||
                        (s.getCustomerName() != null && s.getCustomerName().toLowerCase().contains(k)) ||
                        (s.getReceiverPhone() != null && s.getReceiverPhone().toLowerCase().contains(k)) ||
                        (s.getCurrentLocation() != null && s.getCurrentLocation().toLowerCase().contains(k)))
                .toList();
    }
}