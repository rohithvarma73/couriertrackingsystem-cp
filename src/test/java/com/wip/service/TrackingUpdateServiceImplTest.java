package com.wip.service;

import com.wip.dto.TrackingUpdateDto;
import com.wip.entity.Shipment;
import com.wip.entity.TrackingUpdate;
import com.wip.exception.ResourceNotFoundException;
import com.wip.repository.ShipmentRepository;
import com.wip.repository.TrackingUpdateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingUpdateServiceImplTest {

    @Mock
    private TrackingUpdateRepository trackingUpdateRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    @InjectMocks
    private TrackingUpdateServiceImpl trackingUpdateService;

    @Test
    void testAddTrackingUpdate() {
        Shipment shipment = new Shipment();
        shipment.setShipmentId(1L);

        TrackingUpdate saved = new TrackingUpdate();
        saved.setUpdateId(1L);
        saved.setDeliveryStatus("In-Transit");
        saved.setLocation("Delhi");
        saved.setRemarks("Started");
        saved.setShipment(shipment);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(trackingUpdateRepository.save(org.mockito.ArgumentMatchers.any(TrackingUpdate.class))).thenReturn(saved);

        TrackingUpdateDto input = new TrackingUpdateDto();
        input.setDeliveryStatus("In-Transit");
        input.setLocation("Delhi");
        input.setRemarks("Started");

        TrackingUpdateDto result = trackingUpdateService.addTrackingUpdate(1L, input);

        assertEquals(1L, result.getUpdateId());
        assertEquals(1L, result.getShipmentId());
        assertEquals("In-Transit", result.getDeliveryStatus());
    }

    @Test
    void testGetTrackingUpdatesByShipmentId() {
        Shipment shipment = new Shipment();
        shipment.setShipmentId(1L);

        TrackingUpdate update = new TrackingUpdate();
        update.setUpdateId(1L);
        update.setDeliveryStatus("In-Transit");
        update.setLocation("Delhi");
        update.setRemarks("Started");
        update.setShipment(shipment);

        when(trackingUpdateRepository.findByShipment_ShipmentIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(update));

        List<TrackingUpdateDto> result = trackingUpdateService.getTrackingUpdatesByShipmentId(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUpdateId());
    }

    @Test
    void testGetTrackingUpdatesByShipmentIdEmpty() {
        when(trackingUpdateRepository.findByShipment_ShipmentIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> trackingUpdateService.getTrackingUpdatesByShipmentId(1L));
    }

    @Test
    void testAddTrackingUpdateShipmentNotFound() {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.empty());

        TrackingUpdateDto input = new TrackingUpdateDto();
        input.setDeliveryStatus("In-Transit");
        input.setLocation("Delhi");
        input.setRemarks("Started");

        assertThrows(ResourceNotFoundException.class, () -> trackingUpdateService.addTrackingUpdate(1L, input));
    }
}