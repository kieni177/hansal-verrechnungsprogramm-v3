package com.hansal.verrechnungsprogramm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hansal.verrechnungsprogramm.model.Slaughter;
import com.hansal.verrechnungsprogramm.service.SlaughterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SlaughterController.class)
@WithMockUser
class SlaughterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SlaughterService slaughterService;

    private Slaughter testSlaughter;
    private List<Slaughter> testSlaughters;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        testSlaughter = new Slaughter();
        testSlaughter.setId(1L);
        testSlaughter.setCowTag("AT-1234567");
        testSlaughter.setCowId("COW-001");
        testSlaughter.setSlaughterDate(LocalDate.of(2024, 1, 15));
        testSlaughter.setTotalWeight(new BigDecimal("350.00"));
        testSlaughter.setNotes("Healthy animal");
        testSlaughter.setCreatedAt(LocalDateTime.now());
        testSlaughter.setUpdatedAt(LocalDateTime.now());

        Slaughter slaughter2 = new Slaughter();
        slaughter2.setId(2L);
        slaughter2.setCowTag("AT-7654321");
        slaughter2.setCowId("COW-002");
        slaughter2.setSlaughterDate(LocalDate.of(2024, 1, 20));
        slaughter2.setTotalWeight(new BigDecimal("420.00"));

        testSlaughters = Arrays.asList(testSlaughter, slaughter2);
    }

    @Test
    @DisplayName("GET /api/slaughters - Should return all slaughters")
    void getAllSlaughters_ShouldReturnAllSlaughters() throws Exception {
        when(slaughterService.getAllSlaughters()).thenReturn(testSlaughters);

        mockMvc.perform(get("/api/slaughters"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].cowTag", is("AT-1234567")))
                .andExpect(jsonPath("$[1].cowTag", is("AT-7654321")));

        verify(slaughterService, times(1)).getAllSlaughters();
    }

    @Test
    @DisplayName("GET /api/slaughters/{id} - Should return slaughter by ID")
    void getSlaughterById_ShouldReturnSlaughter() throws Exception {
        when(slaughterService.getSlaughterById(1L)).thenReturn(testSlaughter);

        mockMvc.perform(get("/api/slaughters/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.cowTag", is("AT-1234567")))
                .andExpect(jsonPath("$.cowId", is("COW-001")));

        verify(slaughterService, times(1)).getSlaughterById(1L);
    }

    @Test
    @DisplayName("GET /api/slaughters/search - Should search slaughters by cow tag")
    void searchSlaughters_ShouldReturnMatchingSlaughters() throws Exception {
        when(slaughterService.searchByCowTag("AT-123")).thenReturn(List.of(testSlaughter));

        mockMvc.perform(get("/api/slaughters/search")
                        .param("cowTag", "AT-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cowTag", is("AT-1234567")));

        verify(slaughterService, times(1)).searchByCowTag("AT-123");
    }

    @Test
    @DisplayName("GET /api/slaughters/date-range - Should return slaughters in date range")
    void getSlaughtersByDateRange_ShouldReturnSlaughtersInRange() throws Exception {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        when(slaughterService.getSlaughtersByDateRange(startDate, endDate)).thenReturn(testSlaughters);

        mockMvc.perform(get("/api/slaughters/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(slaughterService, times(1)).getSlaughtersByDateRange(startDate, endDate);
    }

    @Test
    @DisplayName("POST /api/slaughters - Should create new slaughter")
    void createSlaughter_ShouldReturnCreatedSlaughter() throws Exception {
        when(slaughterService.createSlaughter(any(Slaughter.class))).thenReturn(testSlaughter);

        Slaughter newSlaughter = new Slaughter();
        newSlaughter.setCowTag("AT-1234567");
        newSlaughter.setSlaughterDate(LocalDate.of(2024, 1, 15));

        mockMvc.perform(post("/api/slaughters")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSlaughter)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.cowTag", is("AT-1234567")));

        verify(slaughterService, times(1)).createSlaughter(any(Slaughter.class));
    }

    @Test
    @DisplayName("PUT /api/slaughters/{id} - Should update slaughter")
    void updateSlaughter_ShouldReturnUpdatedSlaughter() throws Exception {
        Slaughter updatedSlaughter = new Slaughter();
        updatedSlaughter.setId(1L);
        updatedSlaughter.setCowTag("AT-1234567-UPDATED");
        updatedSlaughter.setCowId("COW-001-UPDATED");
        updatedSlaughter.setSlaughterDate(LocalDate.of(2024, 1, 15));
        updatedSlaughter.setTotalWeight(new BigDecimal("360.00"));

        when(slaughterService.updateSlaughter(eq(1L), any(Slaughter.class))).thenReturn(updatedSlaughter);

        mockMvc.perform(put("/api/slaughters/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedSlaughter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cowTag", is("AT-1234567-UPDATED")))
                .andExpect(jsonPath("$.totalWeight", is(360.00)));

        verify(slaughterService, times(1)).updateSlaughter(eq(1L), any(Slaughter.class));
    }

    @Test
    @DisplayName("DELETE /api/slaughters/{id} - Should delete slaughter")
    void deleteSlaughter_ShouldReturnNoContent() throws Exception {
        doNothing().when(slaughterService).deleteSlaughter(1L);

        mockMvc.perform(delete("/api/slaughters/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(slaughterService, times(1)).deleteSlaughter(1L);
    }

    @Test
    @DisplayName("GET /api/slaughters - Should return empty list when no slaughters")
    void getAllSlaughters_WhenNoSlaughters_ShouldReturnEmptyList() throws Exception {
        when(slaughterService.getAllSlaughters()).thenReturn(List.of());

        mockMvc.perform(get("/api/slaughters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(slaughterService, times(1)).getAllSlaughters();
    }

    @Test
    @DisplayName("GET /api/slaughters/search - Should return empty list when no match")
    void searchSlaughters_WhenNoMatch_ShouldReturnEmptyList() throws Exception {
        when(slaughterService.searchByCowTag("NONEXISTENT")).thenReturn(List.of());

        mockMvc.perform(get("/api/slaughters/search")
                        .param("cowTag", "NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(slaughterService, times(1)).searchByCowTag("NONEXISTENT");
    }
}
