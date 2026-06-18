package com.wip.repository;

import com.wip.entity.TrackingUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TrackingUpdateRepository extends JpaRepository<TrackingUpdate, Long> {
    List<TrackingUpdate> findByShipment_ShipmentIdOrderByCreatedAtAsc(Long shipmentId);
    List<TrackingUpdate> findByCreatedBy_Username(String username);
    Optional<TrackingUpdate> findByUpdateIdAndCreatedBy_Username(Long updateId, String username);

    @Transactional
    void deleteByShipment_ShipmentId(Long shipmentId);
}