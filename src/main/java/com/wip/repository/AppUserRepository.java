package com.wip.repository;

import com.wip.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * AppUserRepository Component.
 * 
 * Handles operations and data related to AppUserRepository.
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<AppUser> findByEmail(String email);
}
