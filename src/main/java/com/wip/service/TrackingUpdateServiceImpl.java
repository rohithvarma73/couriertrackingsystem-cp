package com.wip.service;

import com.wip.dto.TrackingUpdateDto;
import com.wip.entity.AppUser;
import com.wip.entity.Shipment;
import com.wip.entity.TrackingUpdate;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.AppUserRepository;
import com.wip.repository.ShipmentRepository;
import com.wip.repository.TrackingUpdateRepository;
import com.wip.security.CurrentUserUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TrackingUpdateServiceImpl Component.
 * 
 * Handles operations and data related to TrackingUpdateServiceImpl.
 */
@Service
public class TrackingUpdateServiceImpl implements TrackingUpdateService {

    private final TrackingUpdateRepository trackingUpdateRepository;
    private final ShipmentRepository shipmentRepository;
    private final AppUserRepository appUserRepository;

    public TrackingUpdateServiceImpl(TrackingUpdateRepository trackingUpdateRepository,
                                     ShipmentRepository shipmentRepository,
                                     AppUserRepository appUserRepository) {
        this.trackingUpdateRepository = trackingUpdateRepository;
        this.shipmentRepository = shipmentRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public TrackingUpdateDto addTrackingUpdate(Long shipmentId, TrackingUpdateDto trackingUpdateDto) {
        if (!CurrentUserUtil.isAdmin()) {
            throw new IllegalStateException("Only administrators can add tracking updates");
        }

        String username = CurrentUserUtil.getCurrentUsername();
        AppUser currentUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        TrackingUpdate trackingUpdate = new TrackingUpdate();
        trackingUpdate.setShipment(shipment);
        trackingUpdate.setCreatedBy(currentUser);
        trackingUpdate.setDeliveryStatus(trackingUpdateDto.getDeliveryStatus());
        trackingUpdate.setLocation(trackingUpdateDto.getLocation());
        trackingUpdate.setRemarks(trackingUpdateDto.getRemarks());
        trackingUpdate.setCreatedAt(LocalDateTime.now());

        shipment.setCurrentLocation(trackingUpdateDto.getLocation());
        shipmentRepository.save(shipment);

        return toDto(trackingUpdateRepository.save(trackingUpdate));
    }

    @Override
    public List<TrackingUpdateDto> getTrackingUpdatesByShipmentId(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found"));

        if (!CurrentUserUtil.isAdmin()) {
            String username = CurrentUserUtil.getCurrentUsername();
            if (shipment.getParcel() == null || shipment.getParcel().getCustomer() == null || 
                shipment.getParcel().getCustomer().getCreatedBy() == null || 
                !username.equals(shipment.getParcel().getCustomer().getCreatedBy().getUsername())) {
                throw new ResourceNotFoundException("Shipment not found");
            }
        }

        List<TrackingUpdate> updates = trackingUpdateRepository.findByShipment_ShipmentIdOrderByCreatedAtAsc(shipmentId);
        return updates.stream().map(this::toDto).toList();
    }

    @Override
    public TrackingUpdateDto getTrackingUpdateById(Long updateId) {
        TrackingUpdate update = trackingUpdateRepository.findById(updateId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking update not found"));

        if (!CurrentUserUtil.isAdmin()) {
            String username = CurrentUserUtil.getCurrentUsername();
            if (update.getShipment() == null || update.getShipment().getParcel() == null || 
                update.getShipment().getParcel().getCustomer() == null || 
                update.getShipment().getParcel().getCustomer().getCreatedBy() == null || 
                !username.equals(update.getShipment().getParcel().getCustomer().getCreatedBy().getUsername())) {
                throw new ResourceNotFoundException("Tracking update not found");
            }
        }

        return toDto(update);
    }

    @Override
    public void deleteTrackingUpdate(Long updateId) {
        if (!CurrentUserUtil.isAdmin()) {
            throw new IllegalStateException("Only administrators can delete tracking updates");
        }
        TrackingUpdate update = trackingUpdateRepository.findById(updateId)
                .orElseThrow(() -> new ResourceNotFoundException("Tracking update not found"));

        trackingUpdateRepository.delete(update);
    }

    @Override
    public List<TrackingUpdateDto> search(String keyword) {
        List<TrackingUpdateDto> updates = getAllTrackingUpdates();
        if (keyword == null || keyword.isBlank()) {
            return updates;
        }

        String k = keyword.toLowerCase();
        return updates.stream()
                .filter(t ->
                        (t.getUpdateId() != null && String.valueOf(t.getUpdateId()).contains(k)) ||
                        (t.getShipmentId() != null && String.valueOf(t.getShipmentId()).contains(k)) ||
                        (t.getTrackingNumber() != null && t.getTrackingNumber().toLowerCase().contains(k)) ||
                        (t.getDeliveryStatus() != null && t.getDeliveryStatus().toLowerCase().contains(k)) ||
                        (t.getLocation() != null && t.getLocation().toLowerCase().contains(k)) ||
                        (t.getRemarks() != null && t.getRemarks().toLowerCase().contains(k)))
                .toList();
    }

    @Override
    public List<TrackingUpdateDto> getAllTrackingUpdates() {
        if (CurrentUserUtil.isAdmin()) {
            return trackingUpdateRepository.findAll().stream().map(this::toDto).toList();
        }
        String username = CurrentUserUtil.getCurrentUsername();
        return trackingUpdateRepository.findByShipment_Parcel_Customer_CreatedBy_Username(username)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private TrackingUpdateDto toDto(TrackingUpdate update) {
        TrackingUpdateDto dto = new TrackingUpdateDto();
        dto.setUpdateId(update.getUpdateId());
        dto.setShipmentId(update.getShipment() != null ? update.getShipment().getShipmentId() : null);
        dto.setTrackingNumber(update.getShipment() != null ? update.getShipment().getTrackingNumber() : null);
        dto.setDeliveryStatus(update.getDeliveryStatus());
        dto.setLocation(update.getLocation());
        dto.setRemarks(update.getRemarks());
        dto.setCreatedAt(update.getCreatedAt());
        return dto;
    }
}
