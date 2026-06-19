package com.wip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.dto.TrackingUpdateDto;
import com.wip.exception.ResourceNotFoundException;
import com.wip.security.CustomUserDetailsService;
import com.wip.service.TrackingUpdateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrackingUpdateController.class)
class TrackingUpdateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrackingUpdateService trackingUpdateService;

    // Required by SecurityConfig
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    // ── GET /api/shipments/{shipmentId}/tracking-updates ──────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByShipmentId_existing_returns200WithList() throws Exception {
        TrackingUpdateDto dto = makeDto(1L, 10L, "In Transit", "Mumbai Hub", "On the way", LocalDateTime.now());
        when(trackingUpdateService.getTrackingUpdatesByShipmentId(10L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/shipments/10/tracking-updates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].updateId").value(1))
                .andExpect(jsonPath("$[0].deliveryStatus").value("In Transit"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByShipmentId_noUpdates_returns200WithEmptyList() throws Exception {
        when(trackingUpdateService.getTrackingUpdatesByShipmentId(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/shipments/10/tracking-updates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByShipmentId_shipmentNotFound_returns404() throws Exception {
        when(trackingUpdateService.getTrackingUpdatesByShipmentId(999L))
                .thenThrow(new ResourceNotFoundException("Shipment not found"));

        mockMvc.perform(get("/api/shipments/999/tracking-updates"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/shipments/{shipmentId}/tracking-updates ─────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void addUpdate_validPayload_returns200() throws Exception {
        TrackingUpdateDto request = new TrackingUpdateDto();
        request.setDeliveryStatus("Delivered");
        request.setLocation("Customer Address");
        request.setRemarks("Delivered successfully");

        TrackingUpdateDto response = makeDto(5L, 10L, "Delivered", "Customer Address",
                "Delivered successfully", LocalDateTime.now());
        when(trackingUpdateService.addTrackingUpdate(anyLong(), any(TrackingUpdateDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/shipments/10/tracking-updates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updateId").value(5))
                .andExpect(jsonPath("$.deliveryStatus").value("Delivered"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addUpdate_missingDeliveryStatus_returns400() throws Exception {
        TrackingUpdateDto request = new TrackingUpdateDto();
        // deliveryStatus intentionally omitted
        request.setLocation("Some Location");

        mockMvc.perform(post("/api/shipments/10/tracking-updates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addUpdate_missingLocation_returns400() throws Exception {
        TrackingUpdateDto request = new TrackingUpdateDto();
        request.setDeliveryStatus("In Transit");
        // location intentionally omitted

        mockMvc.perform(post("/api/shipments/10/tracking-updates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addUpdate_shipmentNotFound_returns404() throws Exception {
        TrackingUpdateDto request = new TrackingUpdateDto();
        request.setDeliveryStatus("In Transit");
        request.setLocation("Hub A");

        when(trackingUpdateService.addTrackingUpdate(anyLong(), any(TrackingUpdateDto.class)))
                .thenThrow(new ResourceNotFoundException("Shipment not found"));

        mockMvc.perform(post("/api/shipments/999/tracking-updates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── auth guard ────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "USER")
    void getByShipmentId_asUser_returns200() throws Exception {
        // Users are allowed to view tracking
        when(trackingUpdateService.getTrackingUpdatesByShipmentId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/shipments/1/tracking-updates"))
                .andExpect(status().isOk());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private TrackingUpdateDto makeDto(Long id, Long shipmentId, String status,
                                     String location, String remarks, LocalDateTime createdAt) {
        TrackingUpdateDto dto = new TrackingUpdateDto();
        dto.setUpdateId(id);
        dto.setShipmentId(shipmentId);
        dto.setDeliveryStatus(status);
        dto.setLocation(location);
        dto.setRemarks(remarks);
        dto.setCreatedAt(createdAt);
        return dto;
    }
}