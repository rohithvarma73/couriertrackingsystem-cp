package com.wip.service;

import com.wip.dto.ShipmentDto;
import java.util.List;

/**
 * Service interface for managing shipments.
 *
 * <p>Defines the contract for creating, retrieving, updating, and deleting
 * shipment records in the Courier Tracking System.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public interface ShipmentService {

    /**
     * Creates a new shipment for an existing parcel.
     *
     * @param parcelId the ID of the parcel to ship
     * @return the created shipment details
     */
    ShipmentDto addShipment(Long parcelId);

    /**
     * Retrieves all shipments in the system.
     *
     * @return a list of all shipments
     */
    List<ShipmentDto> getAllShipments();

    /**
     * Retrieves a shipment by its unique ID.
     *
     * @param id the unique identifier of the shipment
     * @return the shipment details
     */
    ShipmentDto getShipmentById(Long id);

    /**
     * Retrieves a shipment by its tracking number.
     *
     * @param trackingNumber the unique alphanumeric tracking number
     * @return the shipment details
     */
    ShipmentDto getShipmentByTrackingNumber(String trackingNumber);

    /**
     * Updates the current location of a shipment.
     *
     * @param id              the unique identifier of the shipment
     * @param currentLocation the new location of the shipment
     * @return the updated shipment details
     */
    ShipmentDto updateShipmentLocation(Long id, String currentLocation);

    /**
     * Updates shipment details such as dates and location.
     *
     * @param id          the unique identifier of the shipment
     * @param shipmentDto the updated shipment details
     * @return the updated shipment details
     */
    ShipmentDto updateShipment(Long id, ShipmentDto shipmentDto);

    /**
     * Retrieves the shipment associated with a specific parcel.
     *
     * @param parcelId the unique identifier of the parcel
     * @return the associated shipment, or null if not found
     */
    ShipmentDto getShipmentByParcelId(Long parcelId);

    /**
     * Deletes a shipment from the system.
     *
     * @param id the unique identifier of the shipment to delete
     */
    void deleteShipment(Long id);

    /**
     * Searches for shipments using a keyword.
     *
     * @param keyword the search string
     * @return a list of shipments matching the criteria
     */
    List<ShipmentDto> search(String keyword);
}
