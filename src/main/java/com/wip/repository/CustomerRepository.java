package com.wip.repository;

import com.wip.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Customer} entity persistence operations.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD and pagination operations
 * on the {@code customer} table. Provides a custom JPQL query to determine whether
 * any parcels are linked to a customer (used to prevent deletion of customers with
 * existing bookings), and a derived finder method to retrieve all customers created
 * by a specific user (for role-based data scoping).</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Checks whether any {@link com.wip.entity.Parcel} records are associated with
     * the given customer ID using a JPQL count query.
     *
     * <p>This is used before deleting a customer to ensure referential integrity;
     * customers with existing parcel bookings must not be deleted.</p>
     *
     * @param customerId the ID of the customer to check
     * @return {@code true} if at least one parcel exists for this customer; {@code false} otherwise
     */
    @Query("select count(p) > 0 from Parcel p where p.customer.customerId = :customerId")
    boolean existsParcelsByCustomerId(@Param("customerId") Long customerId);

    /**
     * Retrieves all {@link Customer} records created by the user with the specified username.
     *
     * <p>Used to enforce data scoping for non-admin users, who should only see
     * customer records that they themselves have created.</p>
     *
     * @param username the username of the creating {@link com.wip.entity.AppUser}
     * @return a list of customers created by the given user; empty if none found
     */
    List<Customer> findByCreatedBy_Username(String username);
}
