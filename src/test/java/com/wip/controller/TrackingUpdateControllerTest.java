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

/**
 * Unit tests for {@link TrackingUpdateController} covering all REST endpoints.
 *
 * <p>Uses {@link WebMvcTest} to load only the web layer slice, with
 * {@link TrackingUpdateService} mocked via Mockito. Tests validate retrieval of
 * tracking updates for a given shipment (including empty list and shipment-not-found
 * scenarios), addition of new tracking updates with valid and invalid payloads, and
 * role-based access control confirming that both {@code ADMIN} and {@code USER}
 * roles can view tracking information.</p>
 *
 * @author Dharshan K S
 * @version 1.0
 * @since 1.0
 */
@WebMvcTest(TrackingUpdateController.class)
class TrackingUpdateControllerTest {

    /** MockMvc instance used to perform HTTP requests against the controller. */
    @Autowired
    private MockMvc mockMvc;

    /** Mocked {@link TrackingUpdateService} dependency injected into the controller under test. */
    @MockitoBean
    private TrackingUpdateService trackingUpdateService;

    // Required by SecurityConfig
    /** Mocked {@link CustomUserDetailsService} required by the Spring Security configuration. */
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    /** Jackson {@link ObjectMapper} used to serialise request bodies to JSON. */
    @Autowired
    private ObjectMapper objectMapper;

    // ── GET /api/shipments/{shipmentId}/tracking-updates ──────────────────────

    /**
     * Verifies that {@code GET /api/shipments/{shipmentId}/tracking-updates} returns
     * HTTP 200 with a non-empty JSON array containing the update ID and delivery status
     * when tracking updates exist for the given shipment.
     *
     * @throws Exception if MockMvc request processing fails
     */
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

    /**
     * Verifies that {@code GET /api/shipments/{shipmentId}/tracking-updates} returns
     * HTTP 200 with an empty JSON array when no tracking updates have been recorded
     * for the given shipment.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getByShipmentId_noUpdates_returns200WithEmptyList() throws Exception {
        when(trackingUpdateService.getTrackingUpdatesByShipmentId(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/shipments/10/tracking-updates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    /**
     * Verifies that {@code GET /api/shipments/{shipmentId}/tracking-updates} returns
     * HTTP 404 when the specified shipment ID does not exist, propagating a
     * {@link ResourceNotFoundException} from the service layer.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getByShipmentId_shipmentNotFound_returns404() throws Exception {
        when(trackingUpdateService.getTrackingUpdatesByShipmentId(999L))
                .thenThrow(new ResourceNotFoundException("Shipment not found"));

        mockMvc.perform(get("/api/shipments/999/tracking-updates"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/shipments/{shipmentId}/tracking-updates ─────────────────────

    /**
     * Verifies that {@code POST /api/shipments/{shipmentId}/tracking-updates} returns
     * HTTP 200 with the persisted tracking update (including a generated update ID and
     * delivery status) when all required fields are valid.
     *
     * @throws Exception if MockMvc request processing fails
     */
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

    /**
     * Verifies that {@code POST /api/shipments/{shipmentId}/tracking-updates} returns
     * HTTP 400 when the {@code deliveryStatus} field is omitted, violating the
     * {@code @NotBlank} Bean Validation constraint on the tracking update DTO.
     *
     * @throws Exception if MockMvc request processing fails
     */
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

    /**
     * Verifies that {@code POST /api/shipments/{shipmentId}/tracking-updates} returns
     * HTTP 400 when the {@code location} field is omitted, violating the
     * {@code @NotBlank} Bean Validation constraint on the tracking update DTO.
     *
     * @throws Exception if MockMvc request processing fails
     */
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

    /**
     * Verifies that {@code POST /api/shipments/{shipmentId}/tracking-updates} returns
     * HTTP 404 when the specified shipment ID does not exist, propagating a
     * {@link ResourceNotFoundException} from the service layer.
     *
     * @throws Exception if MockMvc request processing fails
     */
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

    /**
     * Verifies that {@code GET /api/shipments/{shipmentId}/tracking-updates} returns
     * HTTP 200 when a caller with the {@code USER} role makes the request, confirming
     * that regular users are permitted to view tracking information.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "USER")
    void getByShipmentId_asUser_returns200() throws Exception {
        // Users are allowed to view tracking
        when(trackingUpdateService.getTrackingUpdatesByShipmentId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/shipments/1/tracking-updates"))
                .andExpect(status().isOk());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    /**
     * Constructs a {@link TrackingUpdateDto} with the supplied field values for use in tests.
     *
     * @param id         the unique update ID
     * @param shipmentId the ID of the associated shipment
     * @param status     the delivery status label (e.g., "In Transit", "Delivered")
     * @param location   the physical location at the time of the update
     * @param remarks    free-text remarks describing the update event
     * @param createdAt  the timestamp when the tracking update was created
     * @return a fully populated {@link TrackingUpdateDto} instance
     */
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