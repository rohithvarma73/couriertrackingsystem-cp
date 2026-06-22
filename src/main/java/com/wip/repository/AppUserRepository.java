package com.wip.repository;

import com.wip.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link AppUser} entity persistence operations.
 *
 * <p>Extends {@link JpaRepository} to inherit standard CRUD and pagination operations
 * on the {@code users} table. Additionally exposes custom finder methods for
 * retrieving users by username or email, and existence checks used during user
 * registration to prevent duplicate accounts.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Retrieves an {@link AppUser} by their unique username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the matching user, or empty if not found
     */
    Optional<AppUser> findByUsername(String username);

    /**
     * Checks whether an {@link AppUser} with the given username already exists.
     *
     * <p>Used during registration to enforce username uniqueness before attempting
     * to persist a new user record.</p>
     *
     * @param username the username to check for existence
     * @return {@code true} if a user with the given username exists; {@code false} otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether an {@link AppUser} with the given email address already exists.
     *
     * <p>Used during registration to enforce email uniqueness before attempting
     * to persist a new user record.</p>
     *
     * @param email the email address to check for existence
     * @return {@code true} if a user with the given email exists; {@code false} otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Retrieves an {@link AppUser} by their unique email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the matching user, or empty if not found
     */
    Optional<AppUser> findByEmail(String email);
}
