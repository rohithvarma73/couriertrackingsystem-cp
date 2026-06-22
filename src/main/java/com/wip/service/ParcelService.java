package com.wip.service;

import com.wip.dto.ParcelDto;
import java.util.List;

/**
 * Service interface defining the business operations for managing parcel bookings.
 *
 * <p>This interface provides the contract for creating, retrieving, updating, deleting,
 * and searching parcel records within the courier tracking system. Implementations must
 * enforce role-based access control: non-admin users can only access parcels they have
 * created or that belong to their own customer profile, while administrators have
 * unrestricted access. Deleting a parcel also cascades to remove any associated
 * shipment record.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public interface ParcelService {

    /**
     * Creates and persists a new parcel booking using the details from the provided DTO.
     *
     * <p>For admin users, the target customer is taken from {@code parcelDto.getCustomerId()}.
     * For regular users, the customer is resolved from the authenticated user's own profile.
     * The receiver phone is automatically sourced from the customer's phone number.</p>
     *
     * @param parcelDto the data transfer object containing the new parcel's details;
     *                  must not be {@code null}
     * @return a {@link ParcelDto} representing the newly created parcel, including the
     *         server-assigned {@code parcelId}
     * @throws com.wip.exception.ResourceNotFoundException if the target customer is not found
     *         or is not accessible to the current user
     */
    ParcelDto addParcel(ParcelDto parcelDto);

    /**
     * Retrieves all parcel records visible to the currently authenticated user.
     *
     * <p>Administrators receive all parcel records. Non-admin users receive only
     * parcels they have created.</p>
     *
     * @return a list of accessible {@link ParcelDto} objects; may be empty
     */
    List<ParcelDto> getAllParcels();

    /**
     * Retrieves a single parcel record by its unique identifier.
     *
     * <p>Non-admin users may only retrieve parcels they have created; attempts to
     * access another user's parcel will result in a not-found exception.</p>
     *
     * @param id the unique ID of the parcel to retrieve
     * @return the matching {@link ParcelDto}
     * @throws com.wip.exception.ResourceNotFoundException if no parcel with the given ID
     *         exists or if the current user does not have access to it
     */
    ParcelDto getParcelById(Long id);

    /**
     * Retrieves all parcels belonging to the specified customer.
     *
     * <p>Non-admin users may only retrieve parcels for customers they created and
     * parcels they themselves booked.</p>
     *
     * @param customerId the unique ID of the customer whose parcels are to be retrieved
     * @return a list of {@link ParcelDto} objects for the given customer; may be empty
     * @throws com.wip.exception.ResourceNotFoundException if the customer is not found
     *         or is not accessible to the current user
     */
    List<ParcelDto> getParcelsByCustomerId(Long customerId);

    /**
     * Updates an existing parcel booking with the details provided in the DTO.
     *
     * <p>Non-admin users may only update parcels they have created and must specify
     * a customer they own. The receiver phone is re-synced from the customer profile.</p>
     *
     * @param id        the unique ID of the parcel to update
     * @param parcelDto the DTO containing the updated field values
     * @return the updated {@link ParcelDto}
     * @throws com.wip.exception.ResourceNotFoundException if the parcel or the specified
     *         customer is not found or is not accessible to the current user
     */
    ParcelDto updateParcel(Long id, ParcelDto parcelDto);

    /**
     * Deletes a parcel record and its associated shipment (if any).
     *
     * <p>Non-admin users may only delete parcels they have created. If a shipment
     * exists for the parcel, it is removed first to maintain referential integrity.</p>
     *
     * @param id the unique ID of the parcel to delete
     * @throws com.wip.exception.ResourceNotFoundException if no parcel with the given ID
     *         exists or if the current user does not have access to it
     */
    void deleteParcel(Long id);

    /**
     * Searches for parcel records matching the given keyword across multiple fields.
     *
     * <p>The search is performed in-memory against the parcel ID, customer ID, customer
     * name, receiver phone, source address, and destination address in a case-insensitive
     * manner. Results are scoped to parcels accessible to the current user. A blank or
     * {@code null} keyword returns all accessible parcels.</p>
     *
     * @param keyword the search term; {@code null} or blank returns all accessible parcels
     * @return a filtered list of matching {@link ParcelDto} objects; may be empty
     */
    List<ParcelDto> search(String keyword);
}
