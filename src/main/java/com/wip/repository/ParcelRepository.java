package com.wip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wip.entity.Parcel;

public interface ParcelRepository extends JpaRepository<Parcel, Long> {
}