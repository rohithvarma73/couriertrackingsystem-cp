package com.wip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wip.dto.ParcelDto;
import com.wip.exception.ResourceNotFoundException;
import com.wip.security.CustomUserDetailsService;
import com.wip.service.ParcelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ParcelController} covering all REST endpoints.
 *
 * <p>Uses {@link WebMvcTest} to load only the web layer slice, with
 * {@link ParcelService} mocked via Mockito. Tests cover successful parcel retrieval,
 * creation and deletion scenarios, as well as Bean Validation failures for missing
 * required fields (weight, source address) and {@link ResourceNotFoundException}
 * propagation when a referenced customer or parcel does not exist.</p>
 *
 * @author Dharshan K S
 * @version 1.0
 * @since 1.0
 */
@WebMvcTest(ParcelController.class)
class ParcelControllerTest {

    /** MockMvc instance used to perform HTTP requests against the controller. */
    @Autowired
    private MockMvc mockMvc;

    /** Mocked {@link ParcelService} dependency injected into the controller under test. */
    @MockitoBean
    private ParcelService parcelService;

    // Required by SecurityConfig
    /** Mocked {@link CustomUserDetailsService} required by the Spring Security configuration. */
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    /** Jackson {@link ObjectMapper} used to serialise request bodies to JSON. */
    @Autowired
    private ObjectMapper objectMapper;

    // ── GET /api/parcels/getAll ───────────────────────────────────────────────

    /**
     * Verifies that {@code GET /api/parcels/getAll} returns HTTP 200 with a non-empty
     * JSON array containing the parcel ID when at least one parcel exists and the
     * caller has the {@code ADMIN} role.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_asAdmin_returns200() throws Exception {
        ParcelDto dto = makeDto(1L, 2L, new BigDecimal("1.5"), "Delhi", "Mumbai", LocalDate.now());
        when(parcelService.getAllParcels()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/parcels/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].parcelId").value(1));
    }

    /**
     * Verifies that {@code GET /api/parcels/getAll} returns HTTP 200 with an empty
     * JSON array when no parcels exist in the system.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_empty_returns200WithEmptyArray() throws Exception {
        when(parcelService.getAllParcels()).thenReturn(List.of());

        mockMvc.perform(get("/api/parcels/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/parcels/{id} ─────────────────────────────────────────────────

    /**
     * Verifies that {@code GET /api/parcels/{id}} returns HTTP 200 with the correct
     * parcel payload when the requested parcel ID exists.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_existing_returns200() throws Exception {
        ParcelDto dto = makeDto(3L, 1L, new BigDecimal("2.0"), "Chennai", "Hyderabad", LocalDate.now());
        when(parcelService.getParcelById(3L)).thenReturn(dto);

        mockMvc.perform(get("/api/parcels/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parcelId").value(3));
    }

    /**
     * Verifies that {@code GET /api/parcels/{id}} returns HTTP 404 when the requested
     * parcel ID does not exist, producing a {@link ResourceNotFoundException}.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_notFound_returns404() throws Exception {
        when(parcelService.getParcelById(999L))
                .thenThrow(new ResourceNotFoundException("Parcel not found"));

        mockMvc.perform(get("/api/parcels/999"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/parcels/addParcel ───────────────────────────────────────────

    /**
     * Verifies that {@code POST /api/parcels/addParcel} returns HTTP 200 with the
     * persisted parcel (including a generated ID) when all required fields are valid.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void addParcel_validPayload_returns200() throws Exception {
        ParcelDto request = makeDto(null, 1L, new BigDecimal("3.0"), "Kolkata", "Pune", LocalDate.now());
        ParcelDto response = makeDto(10L, 1L, new BigDecimal("3.0"), "Kolkata", "Pune", LocalDate.now());

        when(parcelService.addParcel(any(ParcelDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/parcels/addParcel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parcelId").value(10));
    }

    /**
     * Verifies that {@code POST /api/parcels/addParcel} returns HTTP 400 when the
     * {@code weight} field is omitted, violating the {@code @NotNull} Bean Validation
     * constraint on the parcel DTO.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void addParcel_missingWeight_returns400() throws Exception {
        ParcelDto request = new ParcelDto();
        request.setCustomerId(1L);
        request.setSourceAddress("Delhi");
        request.setDestinationAddress("Mumbai");
        request.setBookingDate(LocalDate.now());
        // weight intentionally omitted

        mockMvc.perform(post("/api/parcels/addParcel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that {@code POST /api/parcels/addParcel} returns HTTP 400 when the
     * {@code sourceAddress} field is omitted, violating the {@code @NotBlank} Bean
     * Validation constraint on the parcel DTO.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void addParcel_missingSourceAddress_returns400() throws Exception {
        ParcelDto request = new ParcelDto();
        request.setCustomerId(1L);
        request.setWeight(new BigDecimal("1.0"));
        request.setDestinationAddress("Mumbai");
        request.setBookingDate(LocalDate.now());

        mockMvc.perform(post("/api/parcels/addParcel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that {@code POST /api/parcels/addParcel} returns HTTP 404 when the
     * referenced customer ID does not exist, propagating a {@link ResourceNotFoundException}
     * from the service layer.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void addParcel_customerNotFound_returns404() throws Exception {
        ParcelDto request = makeDto(null, 999L, new BigDecimal("1.0"), "Delhi", "Mumbai", LocalDate.now());

        when(parcelService.addParcel(any(ParcelDto.class)))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        mockMvc.perform(post("/api/parcels/addParcel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/parcels/{id} ──────────────────────────────────────────────

    /**
     * Verifies that {@code DELETE /api/parcels/{id}} returns HTTP 204 No Content
     * when the parcel exists and is successfully deleted.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteParcel_existing_returns204() throws Exception {
        doNothing().when(parcelService).deleteParcel(1L);

        mockMvc.perform(delete("/api/parcels/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * Verifies that {@code DELETE /api/parcels/{id}} returns HTTP 404 when the
     * requested parcel ID does not exist, propagating a {@link ResourceNotFoundException}.
     *
     * @throws Exception if MockMvc request processing fails
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteParcel_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Parcel not found"))
                .when(parcelService).deleteParcel(999L);

        mockMvc.perform(delete("/api/parcels/999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    /**
     * Constructs a {@link ParcelDto} with the supplied field values for use in tests.
     *
     * @param id         the parcel ID (may be {@code null} for creation scenarios)
     * @param customerId the ID of the owning customer
     * @param weight     the parcel weight in kilograms
     * @param src        the source address from which the parcel is dispatched
     * @param dst        the destination address to which the parcel is to be delivered
     * @param date       the booking date for the parcel
     * @return a fully populated {@link ParcelDto} instance
     */
    private ParcelDto makeDto(Long id, Long customerId, BigDecimal weight,
                              String src, String dst, LocalDate date) {
        ParcelDto dto = new ParcelDto();
        dto.setParcelId(id);
        dto.setCustomerId(customerId);
        dto.setWeight(weight);
        dto.setSourceAddress(src);
        dto.setDestinationAddress(dst);
        dto.setBookingDate(date);
        return dto;
    }
}