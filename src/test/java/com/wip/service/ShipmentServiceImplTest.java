package com.wip.service;

import com.wip.dto.ShipmentDto;
import com.wip.entity.AppUser;
import com.wip.entity.Parcel;
import com.wip.entity.Shipment;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.AppUserRepository;
import com.wip.repository.ParcelRepository;
import com.wip.repository.ShipmentRepository;
import com.wip.repository.TrackingUpdateRepository;
import com.wip.security.CurrentUserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ShipmentServiceImpl}.
 *
 * <p>Validates the business logic for creating, retrieving, updating, and deleting
 * shipments. Uses Mockito to mock dependencies like the data repositories and static
 * utility classes (e.g., {@link CurrentUserUtil}) to isolate the service layer during testing.</p>
 *
 * @author Dharshan K S
 * @version 1.0
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private ParcelRepository parcelRepository;
    @Mock private TrackingUpdateRepository trackingUpdateRepository;
    @Mock private AppUserRepository appUserRepository;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    private AppUser adminUser;
    private Parcel parcel;
    private Shipment shipment;

    /**
     * Sets up the test environment before each test.
     */
    @BeforeEach
    void setUp() {
        adminUser = new AppUser();
        adminUser.setUserId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole("ADMIN");

        parcel = new Parcel();
        parcel.setParcelId(5L);
        parcel.setSourceAddress("Delhi");

        shipment = new Shipment();
        shipment.setShipmentId(10L);
        shipment.setTrackingNumber("TRK-ABC12345");
        shipment.setShipmentDate(LocalDate.now());
        shipment.setCurrentLocation("Delhi");
        shipment.setEstimatedDeliveryDate(LocalDate.now().plusDays(3));
        shipment.setParcel(parcel);
        shipment.setCreatedBy(adminUser);
    }

    /**
     * Validates that an admin user can successfully create a new shipment.
     */
    @Test
    void addShipment_asAdmin_createsShipmentWithTrackingNumber() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("admin");

            when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(parcelRepository.findById(5L)).thenReturn(Optional.of(parcel));
            when(shipmentRepository.findByParcel_ParcelId(5L)).thenReturn(Optional.empty());
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

            ShipmentDto result = shipmentService.addShipment(5L);

            assertNotNull(result);
            assertEquals(10L, result.getShipmentId());
            assertNotNull(result.getTrackingNumber());
            assertTrue(result.getTrackingNumber().startsWith("TRK-"));
        }
    }

    /**
     * Validates that a non-admin user is restricted from creating a shipment.
     */
    @Test
    void addShipment_asNonAdmin_throwsIllegalState() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(false);

            assertThrows(IllegalStateException.class, () -> shipmentService.addShipment(5L));
            verify(shipmentRepository, never()).save(any());
        }
    }

    /**
     * Validates that a resource not found exception is thrown when creating a shipment for a non-existent parcel.
     */
    @Test
    void addShipment_parcelNotFound_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("admin");

            when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(parcelRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> shipmentService.addShipment(999L));
            verify(shipmentRepository, never()).save(any());
        }
    }

    /**
     * Validates that an illegal state exception is thrown when creating a duplicate shipment for a parcel.
     */
    @Test
    void addShipment_duplicateShipment_throwsIllegalState() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("admin");

            when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
            when(parcelRepository.findById(5L)).thenReturn(Optional.of(parcel));
            when(shipmentRepository.findByParcel_ParcelId(5L)).thenReturn(Optional.of(shipment));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> shipmentService.addShipment(5L));
            assertTrue(ex.getMessage().toLowerCase().contains("shipment already exists"));
            verify(shipmentRepository, never()).save(any());
        }
    }

    /**
     * Validates that an admin can fetch a shipment by ID.
     */
    @Test
    void getShipmentById_asAdmin_returnsShipment() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(shipmentRepository.findById(10L)).thenReturn(Optional.of(shipment));

            ShipmentDto result = shipmentService.getShipmentById(10L);

            assertEquals(10L, result.getShipmentId());
            assertEquals("TRK-ABC12345", result.getTrackingNumber());
        }
    }

    /**
     * Validates that a resource not found exception is thrown when fetching a non-existent shipment by ID.
     */
    @Test
    void getShipmentById_notFound_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> shipmentService.getShipmentById(999L));
        }
    }

    /**
     * Validates that a shipment can be successfully fetched using its tracking number.
     */
    @Test
    void getByTrackingNumber_asAdmin_returnsShipment() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(shipmentRepository.findByTrackingNumber("TRK-ABC12345"))
                    .thenReturn(Optional.of(shipment));

            ShipmentDto result = shipmentService.getShipmentByTrackingNumber("TRK-ABC12345");

            assertEquals("TRK-ABC12345", result.getTrackingNumber());
        }
    }

    /**
     * Validates that a resource not found exception is thrown for an invalid tracking number.
     */
    @Test
    void getByTrackingNumber_notFound_throwsException() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(shipmentRepository.findByTrackingNumber("TRK-INVALID"))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> shipmentService.getShipmentByTrackingNumber("TRK-INVALID"));
        }
    }

    /**
     * Validates that an admin user can update the current location of a shipment.
     */
    @Test
    void updateLocation_asAdmin_updatesSuccessfully() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);

            Shipment updated = new Shipment();
            updated.setShipmentId(10L);
            updated.setTrackingNumber("TRK-ABC12345");
            updated.setCurrentLocation("Mumbai Hub");
            updated.setParcel(parcel);

            when(shipmentRepository.findById(10L)).thenReturn(Optional.of(shipment));
            when(shipmentRepository.save(any(Shipment.class))).thenReturn(updated);

            ShipmentDto result = shipmentService.updateShipmentLocation(10L, "Mumbai Hub");

            assertEquals("Mumbai Hub", result.getCurrentLocation());
        }
    }

    /**
     * Validates that a non-admin user is restricted from updating a shipment's location.
     */
    @Test
    void updateLocation_asNonAdmin_throwsIllegalState() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(false);

            assertThrows(IllegalStateException.class,
                    () -> shipmentService.updateShipmentLocation(10L, "Mumbai Hub"));
            verify(shipmentRepository, never()).save(any());
        }
    }

    /**
     * Validates that fetching all shipments returns the complete list.
     */
    @Test
    void getAllShipments_asAdmin_returnsAll() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(shipmentRepository.findAll()).thenReturn(List.of(shipment));

            List<ShipmentDto> result = shipmentService.getAllShipments();

            assertEquals(1, result.size());
        }
    }

    /**
     * Validates that an empty list is returned when there are no shipments.
     */
    @Test
    void getAllShipments_emptyDatabase_returnsEmptyList() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            when(shipmentRepository.findAll()).thenReturn(List.of());

            List<ShipmentDto> result = shipmentService.getAllShipments();

            assertTrue(result.isEmpty());
        }
    }
}