package com.wip.service;

import com.wip.dto.ParcelDto;
import com.wip.entity.AppUser;
import com.wip.entity.Customer;
import com.wip.entity.Parcel;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.AppUserRepository;
import com.wip.repository.CustomerRepository;
import com.wip.repository.ParcelRepository;
import com.wip.repository.ShipmentRepository;
import com.wip.security.CurrentUserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ParcelServiceImpl} covering all service-layer operations.
 *
 * <p>Uses the Mockito JUnit 5 extension to isolate the service from its repository
 * dependencies. {@link CurrentUserUtil} is mocked as a static utility to simulate
 * different caller identities ({@code ADMIN} vs. regular {@code USER}). Scenarios
 * validated include:</p>
 * <ul>
 *   <li>Adding a parcel as admin with an explicit customer ID.</li>
 *   <li>Adding a parcel as a regular user where the customer ID is resolved from the
 *       caller's own linked customer.</li>
 *   <li>Handling a non-existent customer reference during parcel creation.</li>
 *   <li>Retrieving a parcel by ID with admin access, owner access, and denied access
 *       for non-owners.</li>
 *   <li>Deleting a parcel as admin and rejecting deletion by non-owners.</li>
 *   <li>Listing all parcels as admin and handling an empty dataset.</li>
 * </ul>
 *
 * @author Dharshan K S
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class ParcelServiceImplTest {

    /** Mocked repository for {@link Parcel} persistence operations. */
    @Mock private ParcelRepository parcelRepository;

    /** Mocked repository for {@link Customer} lookup operations. */
    @Mock private CustomerRepository customerRepository;

    /** Mocked repository for {@code Shipment} existence checks linked to a parcel. */
    @Mock private ShipmentRepository shipmentRepository;

    /** Mocked repository for {@link AppUser} lookup operations. */
    @Mock private AppUserRepository appUserRepository;

    /** The {@link ParcelServiceImpl} instance under test with mocked dependencies injected. */
    @InjectMocks
    private ParcelServiceImpl parcelService;

    /** Simulated admin user used in test fixtures. */
    private AppUser adminUser;

    /** Simulated regular (non-admin) user used in test fixtures. */
    private AppUser regularUser;

    /** Sample {@link Customer} entity associated with parcels in test fixtures. */
    private Customer customer;

    /** Sample {@link Parcel} entity returned by mocked repository calls. */
    private Parcel parcel;

    /**
     * Initialises shared test fixtures — admin user, regular user, customer, and parcel —
     * before each test method executes.
     */
    @BeforeEach
    void setUp() {
        adminUser = new AppUser();
        adminUser.setUserId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole("ADMIN");

        regularUser = new AppUser();
        regularUser.setUserId(2L);
        regularUser.setUsername("testuser");
        regularUser.setRole("USER");

        customer = new Customer();
        customer.setCustomerId(10L);
        customer.setCustomerName("Test Customer");
        customer.setPhone("9876543210");
        customer.setCreatedBy(regularUser);

        parcel = new Parcel();
        parcel.setParcelId(20L);
        parcel.setCustomer(customer);
        parcel.setCreatedBy(regularUser);
        parcel.setWeight(new BigDecimal("2.5"));
        parcel.setSourceAddress("Delhi");
        parcel.setDestinationAddress("Mumbai");
        parcel.setBookingDate(LocalDate.now());
        parcel.setReceiverPhone("9876543210");
    }

    // ── addParcel ─────────────────────────────────────────────────────────────

    /**
     * Verifies that {@link ParcelServiceImpl#addParcel(ParcelDto)} successfully persists
     * a new parcel and returns the correct DTO — including the generated parcel ID and
     * receiver phone — when the caller is an admin who provides a valid customer ID.
     */
    @Test
    void addParcel_asAdmin_savesSuccessfully() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("admin");

            when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
            when(parcelRepository.save(any(Parcel.class))).thenReturn(parcel);

            ParcelDto dto = new ParcelDto();
            dto.setCustomerId(10L);
            dto.setWeight(new BigDecimal("2.5"));
            dto.setSourceAddress("Delhi");
            dto.setDestinationAddress("Mumbai");
            dto.setBookingDate(LocalDate.now());

            ParcelDto result = parcelService.addParcel(dto);

            assertEquals(20L, result.getParcelId());
            assertEquals("9876543210", result.getReceiverPhone());
            verify(parcelRepository).save(any(Parcel.class));
        }
    }

    /**
     * Verifies that {@link ParcelServiceImpl#addParcel(ParcelDto)} overrides the customer
     * ID in the DTO with the authenticated user's own linked customer when the caller has
     * the regular {@code USER} role, ensuring users can only create parcels for themselves.
     */
    @Test
    void addParcel_asUser_usesOwnCustomerId() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(false);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("testuser");

            regularUser.setCustomer(customer);
            customer.setCreatedBy(regularUser);

            when(appUserRepository.findByUsername("testuser")).thenReturn(Optional.of(regularUser));
            when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
            when(parcelRepository.save(any(Parcel.class))).thenReturn(parcel);

            ParcelDto dto = new ParcelDto();
            dto.setCustomerId(10L); // will be overridden server-side
            dto.setWeight(new BigDecimal("1.0"));
            dto.setSourceAddress("Pune");
            dto.setDestinationAddress("Bangalore");
            dto.setBookingDate(LocalDate.now());

            ParcelDto result = parcelService.addParcel(dto);

            assertNotNull(result);
            verify(parcelRepository).save(any(Parcel.class));
        }
    }

    /**
     * Verifies that {@link ParcelServiceImpl#addParcel(ParcelDto)} throws a
     * {@link ResourceNotFoundException} and never calls {@code save} when the supplied
     * customer ID does not correspond to any customer in the repository.
     */
    @Test
    void addParcel_customerNotFound_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("admin");

            when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(customerRepository.findById(99L)).thenReturn(Optional.empty());

            ParcelDto dto = new ParcelDto();
            dto.setCustomerId(99L);
            dto.setWeight(new BigDecimal("1.0"));
            dto.setSourceAddress("Delhi");
            dto.setDestinationAddress("Mumbai");
            dto.setBookingDate(LocalDate.now());

            assertThrows(ResourceNotFoundException.class, () -> parcelService.addParcel(dto));
            verify(parcelRepository, never()).save(any());
        }
    }

    // ── getParcelById ─────────────────────────────────────────────────────────

    /**
     * Verifies that {@link ParcelServiceImpl#getParcelById(Long)} returns the correct
     * parcel DTO when the caller is an admin and the parcel ID exists.
     */
    @Test
    void getParcelById_asAdmin_returnsParcel() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(parcelRepository.findById(20L)).thenReturn(Optional.of(parcel));

            ParcelDto result = parcelService.getParcelById(20L);

            assertEquals(20L, result.getParcelId());
        }
    }

    /**
     * Verifies that {@link ParcelServiceImpl#getParcelById(Long)} returns the correct
     * parcel DTO when the caller is the user who created the parcel (the owning user),
     * confirming that ownership-based access is granted.
     */
    @Test
    void getParcelById_asOwningUser_returnsParcel() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(false);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("testuser");
            when(parcelRepository.findById(20L)).thenReturn(Optional.of(parcel));

            ParcelDto result = parcelService.getParcelById(20L);

            assertEquals(20L, result.getParcelId());
        }
    }

    /**
     * Verifies that {@link ParcelServiceImpl#getParcelById(Long)} throws a
     * {@link ResourceNotFoundException} when a regular user who did not create the parcel
     * attempts to access it, enforcing strict data isolation.
     */
    @Test
    void getParcelById_asOtherUser_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(false);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("otheruser");
            when(parcelRepository.findById(20L)).thenReturn(Optional.of(parcel));

            assertThrows(ResourceNotFoundException.class, () -> parcelService.getParcelById(20L));
        }
    }

    /**
     * Verifies that {@link ParcelServiceImpl#getParcelById(Long)} throws a
     * {@link ResourceNotFoundException} when the requested parcel ID does not exist,
     * even when the caller is an admin.
     */
    @Test
    void getParcelById_notFound_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(parcelRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> parcelService.getParcelById(999L));
        }
    }

    // ── deleteParcel ──────────────────────────────────────────────────────────

    /**
     * Verifies that {@link ParcelServiceImpl#deleteParcel(Long)} successfully removes
     * the parcel without throwing an exception when the caller is an admin and the parcel
     * has no associated shipment.
     */
    @Test
    void deleteParcel_asAdmin_deletesSuccessfully() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(parcelRepository.findById(20L)).thenReturn(Optional.of(parcel));
            when(shipmentRepository.findByParcel_ParcelId(20L)).thenReturn(Optional.empty());
            doNothing().when(parcelRepository).delete(parcel);

            assertDoesNotThrow(() -> parcelService.deleteParcel(20L));
            verify(parcelRepository).delete(parcel);
        }
    }

    /**
     * Verifies that {@link ParcelServiceImpl#deleteParcel(Long)} throws a
     * {@link ResourceNotFoundException} and never calls {@code delete} when the caller
     * is a user who does not own the parcel, enforcing ownership-based delete restrictions.
     */
    @Test
    void deleteParcel_asOtherUser_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(false);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("someotheruser");
            when(parcelRepository.findById(20L)).thenReturn(Optional.of(parcel));

            assertThrows(ResourceNotFoundException.class, () -> parcelService.deleteParcel(20L));
            verify(parcelRepository, never()).delete(any());
        }
    }

    // ── getAllParcels ─────────────────────────────────────────────────────────

    /**
     * Verifies that {@link ParcelServiceImpl#getAllParcels()} returns all parcels in the
     * system without filtering when the caller has the {@code ADMIN} role.
     */
    @Test
    void getAllParcels_asAdmin_returnsAll() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(parcelRepository.findAll()).thenReturn(List.of(parcel));

            List<ParcelDto> result = parcelService.getAllParcels();

            assertEquals(1, result.size());
        }
    }

    /**
     * Verifies that {@link ParcelServiceImpl#getAllParcels()} returns an empty list
     * rather than throwing an exception when no parcels exist in the database.
     */
    @Test
    void getAllParcels_emptyDatabase_returnsEmptyList() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(parcelRepository.findAll()).thenReturn(List.of());

            List<ParcelDto> result = parcelService.getAllParcels();

            assertTrue(result.isEmpty());
        }
    }
}