package com.wip.repository;

import com.wip.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Shipment} entity persistence operations.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD and pagination operations
 * on the {@code shipment} table. Exposes custom derived finder methods to support
 * tracking-number-based lookups, parcel-to-shipment resolution, and role-based data
 * scoping that restricts non-admin users to shipments belonging to their own customers.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    /**
     * Retrieves a {@link Shipment} by its unique tracking number.
     *
     * <p>Used for public shipment tracking lookups where only the tracking number
     * is known to the customer.</p>
     *
     * @param trackingNumber the unique tracking number of the shipment
     * @return an {@link Optional} containing the matching shipment, or empty if not found
     */
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    /**
     * Retrieves the {@link Shipment} associated with a specific parcel ID.
     *
     * <p>Since each parcel can have at most one shipment, this returns an
     * {@link Optional} which is empty if no shipment has been created for the parcel yet.</p>
     *
     * @param parcelId the ID of the parcel whose shipment is to be retrieved
     * @return an {@link Optional} containing the associated shipment, or empty if none exists
     */
    Optional<Shipment> findByParcel_ParcelId(Long parcelId);

    /**
     * Retrieves all {@link Shipment} records created by the user with the specified username.
     *
     * <p>Used to scope shipment visibility for admin users who may have created
     * specific shipments.</p>
     *
     * @param username the username of the creating {@link com.wip.entity.AppUser}
     * @return a list of shipments created by the given user; empty if none found
     */
    List<Shipment> findByCreatedBy_Username(String username);

    /**
     * Retrieves a {@link Shipment} by its ID that was also created by the specified user.
     *
     * <p>Used to verify ownership when an admin user attempts to access a shipment by ID.</p>
     *
     * @param shipmentId the ID of the shipment to retrieve
     * @param username   the username of the user who created the shipment
     * @return an {@link Optional} containing the matching shipment, or empty if not found
     *         or not owned by the given user
     */
    Optional<Shipment> findByShipmentIdAndCreatedBy_Username(Long shipmentId, String username);

    /**
     * Retrieves all {@link Shipment} records for parcels belonging to customers
     * created by the specified user.
     *
     * <p>Traverses the relationship chain {@code Shipment -> Parcel -> Customer ->
     * CreatedBy} to return all shipments that are accessible to a regular (non-admin)
     * user based on their customer ownership.</p>
     *
     * @param username the username of the user who created the parent customers
     * @return a list of shipments associated with the given user's customers; empty if none found
     */
    List<Shipment> findByParcel_Customer_CreatedBy_Username(String username);
}
