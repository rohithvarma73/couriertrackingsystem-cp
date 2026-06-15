package com.wip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.wip.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}