package com.wip.service;

import com.wip.dto.RegisterDto;
import com.wip.entity.AppUser;
import com.wip.repository.AppUserRepository;
import com.wip.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl} covering user registration business logic.
 *
 * <p>Uses the Mockito JUnit 5 extension to isolate the service layer from its
 * dependencies ({@link AppUserRepository}, {@link CustomerRepository}, and
 * {@link PasswordEncoder}). Scenarios validated include successful registration,
 * duplicate username rejection, duplicate email rejection, password/confirm-password
 * mismatch rejection, and enforcement of the immutable {@code USER} role for all
 * self-registered accounts.</p>
 *
 * @author Dharshan K S
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    /** Mocked repository for {@link AppUser} persistence operations. */
    @Mock
    private AppUserRepository appUserRepository;

    /** Mocked repository for {@code Customer} persistence operations (used during registration). */
    @Mock
    private CustomerRepository customerRepository;

    /** Mocked password encoder used to hash plain-text passwords before persistence. */
    @Mock
    private PasswordEncoder passwordEncoder;

    /** The {@link AuthServiceImpl} instance under test with mocked dependencies injected. */
    @InjectMocks
    private AuthServiceImpl authService;

    /** A pre-built {@link RegisterDto} with valid registration data reused across tests. */
    private RegisterDto validDto;

    /**
     * Initialises a valid {@link RegisterDto} before each test so that individual
     * test methods can mutate only the fields they need to exercise.
     */
    @BeforeEach
    void setUp() {
        validDto = new RegisterDto();
        validDto.setUsername("newuser");
        validDto.setPassword("password123");
        validDto.setConfirmPassword("password123");
        validDto.setFullName("New User");
        validDto.setEmail("newuser@example.com");
        validDto.setPhone("9876543210");
        validDto.setAddress("Chennai");
    }

    // ── register success ──────────────────────────────────────────────────────

    /**
     * Verifies that {@link AuthServiceImpl#register(RegisterDto)} completes without
     * throwing an exception when all supplied registration details are valid, and confirms
     * that the user entity is saved and the password is encoded exactly once.
     */
    @Test
    void register_validInput_createsUserAndCustomer() {
        when(appUserRepository.existsByUsername("newuser")).thenReturn(false);
        when(appUserRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$hashed$");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(new AppUser());

        assertDoesNotThrow(() -> authService.register(validDto));
        verify(appUserRepository).save(any(AppUser.class));
        verify(passwordEncoder).encode("password123");
    }

    // ── duplicate username ────────────────────────────────────────────────────

    /**
     * Verifies that {@link AuthServiceImpl#register(RegisterDto)} throws a
     * {@link RuntimeException} whose message references "username" when an account
     * with the same username already exists, and confirms that no user is persisted.
     */
    @Test
    void register_duplicateUsername_throwsRuntimeException() {
        when(appUserRepository.existsByUsername("newuser")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(validDto));
        assertTrue(ex.getMessage().toLowerCase().contains("username"));
        verify(appUserRepository, never()).save(any());
    }

    // ── duplicate email ───────────────────────────────────────────────────────

    /**
     * Verifies that {@link AuthServiceImpl#register(RegisterDto)} throws a
     * {@link RuntimeException} whose message references "email" when an account
     * with the same email address already exists, and confirms that no user is persisted.
     */
    @Test
    void register_duplicateEmail_throwsRuntimeException() {
        when(appUserRepository.existsByUsername("newuser")).thenReturn(false);
        when(appUserRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(validDto));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        verify(appUserRepository, never()).save(any());
    }

    // ── password mismatch ─────────────────────────────────────────────────────

    /**
     * Verifies that {@link AuthServiceImpl#register(RegisterDto)} throws a
     * {@link RuntimeException} whose message references "password" when the
     * {@code password} and {@code confirmPassword} fields do not match, and
     * confirms that no user is persisted.
     */
    @Test
    void register_passwordMismatch_throwsRuntimeException() {
        validDto.setConfirmPassword("differentpassword");

        when(appUserRepository.existsByUsername("newuser")).thenReturn(false);
        when(appUserRepository.existsByEmail("newuser@example.com")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(validDto));
        assertTrue(ex.getMessage().toLowerCase().contains("password"));
        verify(appUserRepository, never()).save(any());
    }

    // ── role is always USER ───────────────────────────────────────────────────

    /**
     * Verifies that {@link AuthServiceImpl#register(RegisterDto)} always assigns the
     * {@code USER} role to a self-registered account, regardless of any role value that
     * might be present in the DTO, enforcing the security principle that only administrators
     * can elevate privileges.
     */
    @Test
    void register_newUser_roleIsAlwaysUser() {
        when(appUserRepository.existsByUsername("newuser")).thenReturn(false);
        when(appUserRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$hashed$");

        // Capture the saved user to verify role
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(inv -> {
            AppUser user = inv.getArgument(0);
            assertEquals("USER", user.getRole(),
                    "Self-registered users must always have role USER");
            user.setUserId(1L);
            return user;
        });

        assertDoesNotThrow(() -> authService.register(validDto));
    }
}
