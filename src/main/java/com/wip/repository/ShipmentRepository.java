package com.wip.repository;

import com.wip.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    Optional<Shipment> findByParcel_ParcelId(Long parcelId);
    List<Shipment> findByCreatedBy_Username(String username);
    Optional<Shipment> findByShipmentIdAndCreatedBy_Username(Long shipmentId, String username);
}