package com.wip.repository;

import com.wip.entity.TrackingUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link TrackingUpdate} entity persistence operations.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD and pagination operations
 * on the {@code tracking_update} table. Provides custom derived finder methods for
 * retrieving tracking events ordered chronologically, scoping results to a specific
 * user's data, and performing bulk deletion of updates when a shipment is removed.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public interface TrackingUpdateRepository extends JpaRepository<TrackingUpdate, Long> {

    /**
     * Retrieves all {@link TrackingUpdate} records for a given shipment, ordered
     * chronologically by their creation timestamp in ascending order.
     *
     * <p>Used to display the complete delivery history of a shipment in the correct
     * chronological sequence.</p>
     *
     * @param shipmentId the ID of the shipment whose tracking updates are to be retrieved
     * @return an ordered list of tracking updates for the given shipment; empty if none found
     */
    List<TrackingUpdate> findByShipment_ShipmentIdOrderByCreatedAtAsc(Long shipmentId);

    /**
     * Retrieves all {@link TrackingUpdate} records created by the user with the
     * specified username.
     *
     * <p>Used for admin-scoped filtering when viewing updates created by a particular
     * administrator.</p>
     *
     * @param username the username of the creating {@link com.wip.entity.AppUser}
     * @return a list of tracking updates created by the given user; empty if none found
     */
    List<TrackingUpdate> findByCreatedBy_Username(String username);

    /**
     * Retrieves a single {@link TrackingUpdate} by its ID that was also created by
     * the specified user.
     *
     * <p>Used to verify ownership when a user attempts to access a specific tracking
     * update record.</p>
     *
     * @param updateId the ID of the tracking update to retrieve
     * @param username the username of the user who created the update
     * @return an {@link Optional} containing the matching tracking update, or empty
     *         if not found or not owned by the given user
     */
    Optional<TrackingUpdate> findByUpdateIdAndCreatedBy_Username(Long updateId, String username);

    /**
     * Retrieves all {@link TrackingUpdate} records for shipments belonging to parcels
     * of customers created by the specified user.
     *
     * <p>Traverses the full relationship chain {@code TrackingUpdate -> Shipment ->
     * Parcel -> Customer -> CreatedBy} to return tracking updates accessible to a
     * regular (non-admin) user based on their customer ownership.</p>
     *
     * @param username the username of the user who created the parent customers
     * @return a list of tracking updates accessible to the given user; empty if none found
     */
    List<TrackingUpdate> findByShipment_Parcel_Customer_CreatedBy_Username(String username);

    /**
     * Deletes all {@link TrackingUpdate} records associated with the given shipment ID.
     *
     * <p>Executed within a transaction to ensure atomicity. Called before deleting a
     * {@link com.wip.entity.Shipment} to remove dependent tracking records and maintain
     * referential integrity.</p>
     *
     * @param shipmentId the ID of the shipment whose tracking updates are to be deleted
     */
    @Transactional
    void deleteByShipment_ShipmentId(Long shipmentId);
}
