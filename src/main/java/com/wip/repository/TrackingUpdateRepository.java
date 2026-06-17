package com.wip.repository;

import com.wip.entity.TrackingUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TrackingUpdateRepository extends JpaRepository<TrackingUpdate, Long> {
    List<TrackingUpdate> findByShipment_ShipmentIdOrderByCreatedAtAsc(Long shipmentId);

    @Transactional
    void deleteByShipment_ShipmentId(Long shipmentId);
}