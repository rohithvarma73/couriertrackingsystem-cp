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

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterDto validDto;

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

    @Test
    void register_duplicateUsername_throwsRuntimeException() {
        when(appUserRepository.existsByUsername("newuser")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(validDto));
        assertTrue(ex.getMessage().toLowerCase().contains("username"));
        verify(appUserRepository, never()).save(any());
    }

    // ── duplicate email ───────────────────────────────────────────────────────

    @Test
    void register_duplicateEmail_throwsRuntimeException() {
        when(appUserRepository.existsByUsername("newuser")).thenReturn(false);
        when(appUserRepository.existsByEmail("newuser@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(validDto));
        assertTrue(ex.getMessage().toLowerCase().contains("email"));
        verify(appUserRepository, never()).save(any());
    }

    // ── password mismatch ─────────────────────────────────────────────────────

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
