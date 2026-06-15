package com.wip.repository;

import com.wip.entity.TrackingUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingUpdateRepository extends JpaRepository<TrackingUpdate, Long> {
    List<TrackingUpdate> findByShipment_ShipmentIdOrderByCreatedAtAsc(Long shipmentId);
}