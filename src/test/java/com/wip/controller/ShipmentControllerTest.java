package com.wip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.dto.ShipmentDto;
import com.wip.exception.ResourceNotFoundException;
import com.wip.security.CustomUserDetailsService;
import com.wip.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShipmentService shipmentService;

    // Required by SecurityConfig
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    // ── GET /api/shipments/getAll ─────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_asAdmin_returns200() throws Exception {
        ShipmentDto dto = makeDto(1L, 2L, "TRK-ABC123", "Mumbai Hub", LocalDate.now().plusDays(3));
        when(shipmentService.getAllShipments()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/shipments/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shipmentId").value(1))
                .andExpect(jsonPath("$[0].trackingNumber").value("TRK-ABC123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_empty_returns200WithEmptyArray() throws Exception {
        when(shipmentService.getAllShipments()).thenReturn(List.of());

        mockMvc.perform(get("/api/shipments/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/shipments/{id} ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_existing_returns200() throws Exception {
        ShipmentDto dto = makeDto(5L, 1L, "TRK-XYZ789", "Delhi Sorting", LocalDate.now().plusDays(2));
        when(shipmentService.getShipmentById(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/shipments/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shipmentId").value(5));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_notFound_returns404() throws Exception {
        when(shipmentService.getShipmentById(999L))
                .thenThrow(new ResourceNotFoundException("Shipment not found"));

        mockMvc.perform(get("/api/shipments/999"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/shipments/tracking/{trackingNumber} ──────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByTrackingNumber_existing_returns200() throws Exception {
        ShipmentDto dto = makeDto(3L, 1L, "TRK-TEST001", "Chennai", LocalDate.now().plusDays(1));
        when(shipmentService.getShipmentByTrackingNumber("TRK-TEST001")).thenReturn(dto);

        mockMvc.perform(get("/api/shipments/tracking/TRK-TEST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").value("TRK-TEST001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByTrackingNumber_notFound_returns404() throws Exception {
        when(shipmentService.getShipmentByTrackingNumber("TRK-INVALID"))
                .thenThrow(new ResourceNotFoundException("Shipment not found"));

        mockMvc.perform(get("/api/shipments/tracking/TRK-INVALID"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/shipments/addShipment/{parcelId} ────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void addShipment_validParcel_returns200WithTrackingNumber() throws Exception {
        ShipmentDto response = makeDto(10L, 2L, "TRK-NEW001", "Bangalore Hub", LocalDate.now().plusDays(3));
        when(shipmentService.addShipment(2L)).thenReturn(response);

        mockMvc.perform(post("/api/shipments/addShipment/2").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingNumber").value("TRK-NEW001"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addShipment_parcelNotFound_returns404() throws Exception {
        when(shipmentService.addShipment(999L))
                .thenThrow(new ResourceNotFoundException("Parcel not found"));

        mockMvc.perform(post("/api/shipments/addShipment/999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addShipment_duplicate_returns400() throws Exception {
        when(shipmentService.addShipment(1L))
                .thenThrow(new IllegalStateException("Shipment already exists for this parcel"));

        mockMvc.perform(post("/api/shipments/addShipment/1").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /api/shipments/{id} ────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteShipment_existing_returns204() throws Exception {
        doNothing().when(shipmentService).deleteShipment(1L);

        mockMvc.perform(delete("/api/shipments/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    // ── PUT /api/shipments/{id}/location ──────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateLocation_existing_returns200() throws Exception {
        ShipmentDto dto = makeDto(1L, 2L, "TRK-LOC001", "New Location Hub", LocalDate.now().plusDays(2));
        when(shipmentService.updateShipmentLocation(1L, "New Location Hub")).thenReturn(dto);

        mockMvc.perform(put("/api/shipments/1/location")
                        .with(csrf())
                        .param("currentLocation", "New Location Hub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentLocation").value("New Location Hub"));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private ShipmentDto makeDto(Long id, Long parcelId, String tracking, String location, LocalDate eta) {
        ShipmentDto dto = new ShipmentDto();
        dto.setShipmentId(id);
        dto.setParcelId(parcelId);
        dto.setTrackingNumber(tracking);
        dto.setCurrentLocation(location);
        dto.setEstimatedDeliveryDate(eta);
        dto.setShipmentDate(LocalDate.now());
        return dto;
    }
}