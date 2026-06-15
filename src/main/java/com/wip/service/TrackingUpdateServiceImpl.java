package com.wip.service;

import com.wip.dto.TrackingUpdateDto;
import com.wip.entity.Shipment;
import com.wip.entity.TrackingUpdate;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.ShipmentRepository;
import com.wip.repository.TrackingUpdateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrackingUpdateServiceImpl implements TrackingUpdateService {

    private final TrackingUpdateRepository trackingUpdateRepository;
    private final ShipmentRepository shipmentRepository;

    public TrackingUpdateServiceImpl(TrackingUpdateRepository trackingUpdateRepository,
                                     ShipmentRepository shipmentRepository) {
        this.trackingUpdateRepository = trackingUpdateRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public TrackingUpdateDto addTrackingUpdate(Long shipmentId, TrackingUpdateDto trackingUpdateDto) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + shipmentId));

        TrackingUpdate update = new TrackingUpdate();
        update.setDeliveryStatus(trackingUpdateDto.getDeliveryStatus());
        update.setLocation(trackingUpdateDto.getLocation());
        update.setRemarks(trackingUpdateDto.getRemarks());
        update.setCreatedAt(LocalDateTime.now());
        update.setShipment(shipment);

        TrackingUpdate saved = trackingUpdateRepository.save(update);
        return toDto(saved);
    }

    @Override
    public TrackingUpdateDto updateTrackingUpdate(Long id, TrackingUpdateDto trackingUpdateDto) {
        TrackingUpdate update = trackingUpdateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking update not found with id: " + id));

        update.setDeliveryStatus(trackingUpdateDto.getDeliveryStatus());
        update.setLocation(trackingUpdateDto.getLocation());
        update.setRemarks(trackingUpdateDto.getRemarks());
        update.setCreatedAt(LocalDateTime.now());

        return toDto(trackingUpdateRepository.save(update));
    }

    @Override
    public List<TrackingUpdateDto> getAllTrackingUpdates() {
        return trackingUpdateRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TrackingUpdateDto getTrackingUpdateById(Long id) {
        TrackingUpdate update = trackingUpdateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking update not found with id: " + id));
        return toDto(update);
    }

    @Override
    public List<TrackingUpdateDto> getTrackingUpdatesByShipmentId(Long shipmentId) {
        List<TrackingUpdate> updates = trackingUpdateRepository.findByShipment_ShipmentIdOrderByCreatedAtAsc(shipmentId);
        if (updates.isEmpty()) {
            throw new ResourceNotFoundException("Update to be yet to updated for shipment id: " + shipmentId);
        }
        return updates.stream().map(this::toDto).collect(Collectors.toList());
    }

    private TrackingUpdateDto toDto(TrackingUpdate update) {
        TrackingUpdateDto dto = new TrackingUpdateDto();
        dto.setUpdateId(update.getUpdateId());
        dto.setDeliveryStatus(update.getDeliveryStatus());
        dto.setLocation(update.getLocation());
        dto.setRemarks(update.getRemarks());
        dto.setCreatedAt(update.getCreatedAt());
        if (update.getShipment() != null) {
            dto.setShipmentId(update.getShipment().getShipmentId());
        }
        return dto;
    }
}