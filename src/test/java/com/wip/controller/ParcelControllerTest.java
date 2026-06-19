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

@WebMvcTest(ParcelController.class)
class ParcelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParcelService parcelService;

    // Required by SecurityConfig
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    // ── GET /api/parcels/getAll ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_asAdmin_returns200() throws Exception {
        ParcelDto dto = makeDto(1L, 2L, new BigDecimal("1.5"), "Delhi", "Mumbai", LocalDate.now());
        when(parcelService.getAllParcels()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/parcels/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].parcelId").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAll_empty_returns200WithEmptyArray() throws Exception {
        when(parcelService.getAllParcels()).thenReturn(List.of());

        mockMvc.perform(get("/api/parcels/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/parcels/{id} ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_existing_returns200() throws Exception {
        ParcelDto dto = makeDto(3L, 1L, new BigDecimal("2.0"), "Chennai", "Hyderabad", LocalDate.now());
        when(parcelService.getParcelById(3L)).thenReturn(dto);

        mockMvc.perform(get("/api/parcels/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parcelId").value(3));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_notFound_returns404() throws Exception {
        when(parcelService.getParcelById(999L))
                .thenThrow(new ResourceNotFoundException("Parcel not found"));

        mockMvc.perform(get("/api/parcels/999"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/parcels/addParcel ───────────────────────────────────────────

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

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteParcel_existing_returns204() throws Exception {
        doNothing().when(parcelService).deleteParcel(1L);

        mockMvc.perform(delete("/api/parcels/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteParcel_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Parcel not found"))
                .when(parcelService).deleteParcel(999L);

        mockMvc.perform(delete("/api/parcels/999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ── helper ────────────────────────────────────────────────────────────────

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