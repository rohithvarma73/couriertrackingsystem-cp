package com.wip.service;

import com.wip.dto.TrackingUpdateDto;
import java.util.List;

public interface TrackingUpdateService {
    TrackingUpdateDto addTrackingUpdate(TrackingUpdateDto trackingUpdateDto);
    List<TrackingUpdateDto> getAllTrackingUpdates();
    TrackingUpdateDto getTrackingUpdateById(Long id);
    TrackingUpdateDto updateTrackingUpdate(Long id, TrackingUpdateDto trackingUpdateDto);
    void deleteTrackingUpdate(Long id);
}