package com.wip.service;

import com.wip.dto.TrackingUpdateDto;

import java.util.List;

/**
 * Service interface for managing tracking updates.
 *
 * <p>Defines the contract for creating, retrieving, and deleting tracking milestones
 * associated with a shipment.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public interface TrackingUpdateService {

    /**
     * Adds a new tracking update to a shipment.
     *
     * @param shipmentId        the unique identifier of the shipment
     * @param trackingUpdateDto the tracking update details
     * @return the created tracking update details
     */
    TrackingUpdateDto addTrackingUpdate(Long shipmentId, TrackingUpdateDto trackingUpdateDto);

    /**
     * Retrieves all tracking updates for a given shipment.
     *
     * @param shipmentId the unique identifier of the shipment
     * @return a chronological list of tracking updates
     */
    List<TrackingUpdateDto> getTrackingUpdatesByShipmentId(Long shipmentId);

    /**
     * Retrieves a tracking update by its ID.
     *
     * @param updateId the unique identifier of the tracking update
     * @return the tracking update details
     */
    TrackingUpdateDto getTrackingUpdateById(Long updateId);

    /**
     * Deletes a tracking update.
     *
     * @param updateId the unique identifier of the tracking update to delete
     */
    void deleteTrackingUpdate(Long updateId);

    /**
     * Searches for tracking updates using a keyword.
     *
     * @param keyword the search string
     * @return a list of tracking updates matching the criteria
     */
    List<TrackingUpdateDto> search(String keyword);

    /**
     * Retrieves all tracking updates in the system.
     *
     * @return a list of all tracking updates
     */
    List<TrackingUpdateDto> getAllTrackingUpdates();
}
