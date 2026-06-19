package com.wip.service;

import com.wip.dto.TrackingUpdateDto;
import com.wip.entity.AppUser;
import com.wip.entity.Shipment;
import com.wip.entity.TrackingUpdate;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.AppUserRepository;
import com.wip.repository.ShipmentRepository;
import com.wip.repository.TrackingUpdateRepository;
import com.wip.security.CurrentUserUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingUpdateServiceImplTest {

    @Mock
    private TrackingUpdateRepository trackingUpdateRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private TrackingUpdateServiceImpl trackingUpdateService;

    @Test
    void testAddTrackingUpdate() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("admin");

            AppUser adminUser = new AppUser();
            adminUser.setUsername("admin");
            when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

            Long shipmentId = 1L;

            Shipment shipment = new Shipment();
            shipment.setShipmentId(shipmentId);
            shipment.setTrackingNumber("TRK123");

            TrackingUpdateDto inputDto = new TrackingUpdateDto();
            inputDto.setDeliveryStatus("In Transit");
            inputDto.setLocation("Hyderabad");
            inputDto.setRemarks("Reached hub");

            TrackingUpdate savedUpdate = new TrackingUpdate();
            savedUpdate.setUpdateId(10L);
            savedUpdate.setShipment(shipment);
            savedUpdate.setDeliveryStatus("In Transit");
            savedUpdate.setLocation("Hyderabad");
            savedUpdate.setRemarks("Reached hub");
            savedUpdate.setCreatedAt(LocalDateTime.now());

            when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
            when(shipmentRepository.save(shipment)).thenReturn(shipment);
            when(trackingUpdateRepository.save(any(TrackingUpdate.class))).thenReturn(savedUpdate);

            TrackingUpdateDto result = trackingUpdateService.addTrackingUpdate(shipmentId, inputDto);

            assertNotNull(result);
            assertEquals(10L, result.getUpdateId());
            assertEquals(shipmentId, result.getShipmentId());
            assertEquals("TRK123", result.getTrackingNumber());
            assertEquals("In Transit", result.getDeliveryStatus());
            assertEquals("Hyderabad", result.getLocation());
            assertEquals("Reached hub", result.getRemarks());

            verify(shipmentRepository).findById(shipmentId);
            verify(shipmentRepository).save(shipment);
            verify(trackingUpdateRepository).save(any(TrackingUpdate.class));
        }
    }

    @Test
    void testAddTrackingUpdateShipmentNotFound() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);
            util.when(CurrentUserUtil::getCurrentUsername).thenReturn("admin");

            AppUser adminUser = new AppUser();
            adminUser.setUsername("admin");
            when(appUserRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

            Long shipmentId = 1L;
            TrackingUpdateDto inputDto = new TrackingUpdateDto();

            when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> trackingUpdateService.addTrackingUpdate(shipmentId, inputDto));

            verify(shipmentRepository).findById(shipmentId);
            verify(trackingUpdateRepository, never()).save(any());
            verify(shipmentRepository, never()).save(any());
        }
    }

    @Test
    void testGetTrackingUpdatesByShipmentId() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);

            Long shipmentId = 1L;

            Shipment shipment = new Shipment();
            shipment.setShipmentId(shipmentId);
            shipment.setTrackingNumber("TRK123");

            TrackingUpdate update1 = new TrackingUpdate();
            update1.setUpdateId(1L);
            update1.setShipment(shipment);
            update1.setDeliveryStatus("Picked");
            update1.setLocation("Chennai");
            update1.setRemarks("Package picked");
            update1.setCreatedAt(LocalDateTime.now());

            TrackingUpdate update2 = new TrackingUpdate();
            update2.setUpdateId(2L);
            update2.setShipment(shipment);
            update2.setDeliveryStatus("In Transit");
            update2.setLocation("Hyderabad");
            update2.setRemarks("Reached hub");
            update2.setCreatedAt(LocalDateTime.now());

            when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
            when(trackingUpdateRepository.findByShipment_ShipmentIdOrderByCreatedAtAsc(shipmentId))
                    .thenReturn(List.of(update1, update2));

            List<TrackingUpdateDto> result = trackingUpdateService.getTrackingUpdatesByShipmentId(shipmentId);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("Picked", result.get(0).getDeliveryStatus());
            assertEquals("In Transit", result.get(1).getDeliveryStatus());

            verify(shipmentRepository).findById(shipmentId);
            verify(trackingUpdateRepository).findByShipment_ShipmentIdOrderByCreatedAtAsc(shipmentId);
        }
    }

    @Test
    void testGetTrackingUpdatesByShipmentIdEmpty() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);

            Long shipmentId = 1L;

            Shipment shipment = new Shipment();
            shipment.setShipmentId(shipmentId);

            when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
            when(trackingUpdateRepository.findByShipment_ShipmentIdOrderByCreatedAtAsc(shipmentId))
                    .thenReturn(Collections.emptyList());

            List<TrackingUpdateDto> result = trackingUpdateService.getTrackingUpdatesByShipmentId(shipmentId);

            assertNotNull(result);
            assertTrue(result.isEmpty());

            verify(shipmentRepository).findById(shipmentId);
            verify(trackingUpdateRepository).findByShipment_ShipmentIdOrderByCreatedAtAsc(shipmentId);
        }
    }

    @Test
    void testGetTrackingUpdatesByShipmentIdShipmentNotFound() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);

            Long shipmentId = 1L;

            when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> trackingUpdateService.getTrackingUpdatesByShipmentId(shipmentId));

            verify(shipmentRepository).findById(shipmentId);
            verify(trackingUpdateRepository, never())
                    .findByShipment_ShipmentIdOrderByCreatedAtAsc(anyLong());
        }
    }

    @Test
    void testGetTrackingUpdateById() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);

            Long updateId = 1L;

            Shipment shipment = new Shipment();
            shipment.setShipmentId(100L);
            shipment.setTrackingNumber("TRK123");

            TrackingUpdate update = new TrackingUpdate();
            update.setUpdateId(updateId);
            update.setShipment(shipment);
            update.setDeliveryStatus("Delivered");
            update.setLocation("Bangalore");
            update.setRemarks("Delivered successfully");
            update.setCreatedAt(LocalDateTime.now());

            when(trackingUpdateRepository.findById(updateId)).thenReturn(Optional.of(update));

            TrackingUpdateDto result = trackingUpdateService.getTrackingUpdateById(updateId);

            assertNotNull(result);
            assertEquals(updateId, result.getUpdateId());
            assertEquals(100L, result.getShipmentId());
            assertEquals("TRK123", result.getTrackingNumber());
            assertEquals("Delivered", result.getDeliveryStatus());

            verify(trackingUpdateRepository).findById(updateId);
        }
    }

    @Test
    void testGetTrackingUpdateByIdNotFound() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);

            Long updateId = 1L;

            when(trackingUpdateRepository.findById(updateId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> trackingUpdateService.getTrackingUpdateById(updateId));

            verify(trackingUpdateRepository).findById(updateId);
        }
    }

    @Test
    void testDeleteTrackingUpdate() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);

            Long updateId = 1L;

            TrackingUpdate update = new TrackingUpdate();
            update.setUpdateId(updateId);

            when(trackingUpdateRepository.findById(updateId)).thenReturn(Optional.of(update));

            trackingUpdateService.deleteTrackingUpdate(updateId);

            verify(trackingUpdateRepository).findById(updateId);
            verify(trackingUpdateRepository).delete(update);
        }
    }

    @Test
    void testDeleteTrackingUpdateNotFound() {
        try (MockedStatic<CurrentUserUtil> util = mockStatic(CurrentUserUtil.class)) {
            util.when(CurrentUserUtil::isAdmin).thenReturn(true);

            Long updateId = 1L;

            when(trackingUpdateRepository.findById(updateId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> trackingUpdateService.deleteTrackingUpdate(updateId));

            verify(trackingUpdateRepository).findById(updateId);
            verify(trackingUpdateRepository, never()).delete(any());
        }
    }
}