package com.wip.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wip.entity.TrackingUpdate;

public interface TrackingUpdateRepository extends JpaRepository<TrackingUpdate, Long> {
    List<TrackingUpdate> findByShipmentShipmentId(Long shipmentId);
}