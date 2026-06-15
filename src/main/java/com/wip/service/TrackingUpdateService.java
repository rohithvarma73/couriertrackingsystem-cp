package com.wip.service;

import com.wip.dto.TrackingUpdateDto;
import java.util.List;

public interface TrackingUpdateService {
    TrackingUpdateDto addTrackingUpdate(Long shipmentId, TrackingUpdateDto trackingUpdateDto);
    TrackingUpdateDto updateTrackingUpdate(Long id, TrackingUpdateDto trackingUpdateDto);
    List<TrackingUpdateDto> getAllTrackingUpdates();
    TrackingUpdateDto getTrackingUpdateById(Long id);
    List<TrackingUpdateDto> getTrackingUpdatesByShipmentId(Long shipmentId);
}