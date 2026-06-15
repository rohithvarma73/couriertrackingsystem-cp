package com.wip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wip.entity.Shipment;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}