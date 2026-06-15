package com.wip.service;

import com.wip.dto.ShipmentDto;
import com.wip.entity.Parcel;
import com.wip.entity.Shipment;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.ParcelRepository;
import com.wip.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ParcelRepository parcelRepository;

    @InjectMocks
    private ShipmentServiceImpl shipmentService;

    @Test
    void testAddShipment() {
        Parcel parcel = new Parcel();
        parcel.setParcelId(1L);
        parcel.setSourceAddress("Delhi");

        Shipment saved = new Shipment();
        saved.setShipmentId(1L);
        saved.setTrackingNumber("TRK-ABC12345");
        saved.setShipmentDate(LocalDate.now());
        saved.setCurrentLocation("Delhi");
        saved.setEstimatedDeliveryDate(LocalDate.now().plusDays(3));
        saved.setParcel(parcel);

        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel));
        when(shipmentRepository.save(org.mockito.ArgumentMatchers.any(Shipment.class))).thenReturn(saved);

        ShipmentDto result = shipmentService.addShipment(1L);

        assertEquals(1L, result.getShipmentId());
        assertEquals("TRK-ABC12345", result.getTrackingNumber());
        assertEquals("Delhi", result.getCurrentLocation());
    }

    @Test
    void testAddShipmentParcelNotFound() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> shipmentService.addShipment(1L));
    }
}