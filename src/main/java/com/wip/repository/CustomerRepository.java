package com.wip.repository;

import com.wip.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("select count(p) > 0 from Parcel p where p.customer.customerId = :customerId")
    boolean existsParcelsByCustomerId(@Param("customerId") Long customerId);

    List<Customer> findByCreatedBy_Username(String username);
}