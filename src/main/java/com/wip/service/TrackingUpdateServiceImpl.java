package com.wip.service;

import com.wip.dto.TrackingUpdateDto;
import com.wip.entity.Shipment;
import com.wip.entity.TrackingUpdate;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.ShipmentRepository;
import com.wip.repository.TrackingUpdateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrackingUpdateServiceImpl implements TrackingUpdateService {

    private final TrackingUpdateRepository trackingUpdateRepository;
    private final ShipmentRepository shipmentRepository;

    public TrackingUpdateServiceImpl(TrackingUpdateRepository trackingUpdateRepository, ShipmentRepository shipmentRepository) {
        this.trackingUpdateRepository = trackingUpdateRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public TrackingUpdateDto addTrackingUpdate(TrackingUpdateDto trackingUpdateDto) {
        TrackingUpdate trackingUpdate = toEntity(trackingUpdateDto);
        Shipment shipment = shipmentRepository.findById(trackingUpdateDto.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + trackingUpdateDto.getShipmentId()));
        trackingUpdate.setShipment(shipment);
        return toDto(trackingUpdateRepository.save(trackingUpdate));
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
        TrackingUpdate trackingUpdate = trackingUpdateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking update not found with id: " + id));
        return toDto(trackingUpdate);
    }

    @Override
    public TrackingUpdateDto updateTrackingUpdate(Long id, TrackingUpdateDto trackingUpdateDto) {
        TrackingUpdate existing = trackingUpdateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking update not found with id: " + id));

        existing.setDeliveryStatus(trackingUpdateDto.getDeliveryStatus());
        existing.setLocation(trackingUpdateDto.getLocation());
        existing.setRemarks(trackingUpdateDto.getRemarks());
        existing.setUpdatedTime(trackingUpdateDto.getUpdatedTime());

        if (trackingUpdateDto.getShipmentId() != null) {
            Shipment shipment = shipmentRepository.findById(trackingUpdateDto.getShipmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + trackingUpdateDto.getShipmentId()));
            existing.setShipment(shipment);
        }

        return toDto(trackingUpdateRepository.save(existing));
    }

    @Override
    public void deleteTrackingUpdate(Long id) {
        TrackingUpdate existing = trackingUpdateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking update not found with id: " + id));
        trackingUpdateRepository.delete(existing);
    }

    private TrackingUpdateDto toDto(TrackingUpdate trackingUpdate) {
        TrackingUpdateDto dto = new TrackingUpdateDto();
        dto.setUpdateId(trackingUpdate.getUpdateId());
        dto.setDeliveryStatus(trackingUpdate.getDeliveryStatus());
        dto.setLocation(trackingUpdate.getLocation());
        dto.setRemarks(trackingUpdate.getRemarks());
        dto.setUpdatedTime(trackingUpdate.getUpdatedTime());
        if (trackingUpdate.getShipment() != null) {
            dto.setShipmentId(trackingUpdate.getShipment().getShipmentId());
        }
        return dto;
    }

    private TrackingUpdate toEntity(TrackingUpdateDto dto) {
        TrackingUpdate trackingUpdate = new TrackingUpdate();
        trackingUpdate.setUpdateId(dto.getUpdateId());
        trackingUpdate.setDeliveryStatus(dto.getDeliveryStatus());
        trackingUpdate.setLocation(dto.getLocation());
        trackingUpdate.setRemarks(dto.getRemarks());
        trackingUpdate.setUpdatedTime(dto.getUpdatedTime());
        return trackingUpdate;
    }
}