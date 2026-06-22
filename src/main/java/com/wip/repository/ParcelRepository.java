package com.wip.repository;

import com.wip.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Parcel} entity persistence operations.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD and pagination operations
 * on the {@code parcel} table. Provides derived finder methods for retrieving parcels
 * scoped to a specific customer or to parcels created by a particular user, enabling
 * role-based data isolation in the service layer.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    /**
     * Retrieves all {@link Parcel} records belonging to the customer with the given ID.
     *
     * <p>Used to list all parcel bookings associated with a specific customer profile.</p>
     *
     * @param customerId the ID of the customer whose parcels are to be retrieved
     * @return a list of parcels for the given customer; empty if none found
     */
    List<Parcel> findByCustomer_CustomerId(Long customerId);

    /**
     * Retrieves all {@link Parcel} records created by the user with the specified username.
     *
     * <p>Used to enforce data scoping for non-admin users, who should only see
     * parcel records that they themselves have created.</p>
     *
     * @param username the username of the creating {@link com.wip.entity.AppUser}
     * @return a list of parcels created by the given user; empty if none found
     */
    List<Parcel> findByCreatedBy_Username(String username);
}
