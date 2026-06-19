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

@ExtendWith(MockitoExtension.class)
class ParcelServiceImplTest {

    @Mock private ParcelRepository parcelRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @Mock private AppUserRepository appUserRepository;

    @InjectMocks
    private ParcelServiceImpl parcelService;

    private AppUser adminUser;
    private AppUser regularUser;
    private Customer customer;
    private Parcel parcel;

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

    @Test
    void getParcelById_asAdmin_returnsParcel() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(parcelRepository.findById(20L)).thenReturn(Optional.of(parcel));

            ParcelDto result = parcelService.getParcelById(20L);

            assertEquals(20L, result.getParcelId());
        }
    }

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

    @Test
    void getParcelById_asOtherUser_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(false);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("otheruser");
            when(parcelRepository.findById(20L)).thenReturn(Optional.of(parcel));

            assertThrows(ResourceNotFoundException.class, () -> parcelService.getParcelById(20L));
        }
    }

    @Test
    void getParcelById_notFound_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(parcelRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> parcelService.getParcelById(999L));
        }
    }

    // ── deleteParcel ──────────────────────────────────────────────────────────

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

    @Test
    void getAllParcels_asAdmin_returnsAll() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(parcelRepository.findAll()).thenReturn(List.of(parcel));

            List<ParcelDto> result = parcelService.getAllParcels();

            assertEquals(1, result.size());
        }
    }

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