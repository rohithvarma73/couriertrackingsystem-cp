package com.wip.repository;

import com.wip.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
    List<Parcel> findByCustomer_CustomerId(Long customerId);
    List<Parcel> findByCreatedBy_Username(String username);
}