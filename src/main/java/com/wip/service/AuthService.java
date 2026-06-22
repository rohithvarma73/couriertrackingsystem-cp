package com.wip.service;

import com.wip.dto.RegisterDto;

/**
 * Service interface defining the authentication operations for the courier tracking system.
 *
 * <p>This interface provides the contract for user self-registration. Implementations
 * are responsible for validating registration data (uniqueness of username/email,
 * password confirmation), encoding credentials, and creating both the
 * {@code AppUser} authentication record and the linked {@code Customer} profile
 * atomically within a single transaction.</p>
 *
 * @author Rohith Varma K
 * @version 1.0
 * @since 1.0
 */
public interface AuthService {

    /**
     * Registers a new user account along with an associated customer profile.
     *
     * <p>Validates that the supplied username and email are not already in use,
     * and that the {@code password} and {@code confirmPassword} values match.
     * On success, both the {@link com.wip.entity.AppUser} record (with an
     * encoded password and {@code USER} role) and the linked
     * {@link com.wip.entity.Customer} profile are persisted atomically.</p>
     *
     * @param registerDto the registration data transfer object containing the new user's
     *                    credentials and personal information; must not be {@code null}
     * @throws RuntimeException if the username is already taken, the email is already
     *                          registered, or the passwords do not match
     */
    void register(RegisterDto registerDto);
}
