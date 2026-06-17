package com.wip.service;

import com.wip.dto.TrackingUpdateDto;

import java.util.List;

public interface TrackingUpdateService {
    TrackingUpdateDto addTrackingUpdate(Long shipmentId, TrackingUpdateDto trackingUpdateDto);

    List<TrackingUpdateDto> getTrackingUpdatesByShipmentId(Long shipmentId);

    TrackingUpdateDto getTrackingUpdateById(Long updateId);

    void deleteTrackingUpdate(Long updateId);

    List<TrackingUpdateDto> search(String keyword);

    List<TrackingUpdateDto> getAllTrackingUpdates();
}