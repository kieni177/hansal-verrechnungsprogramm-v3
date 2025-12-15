package com.hansal.verrechnungsprogramm.controller;

import com.hansal.verrechnungsprogramm.service.DatabaseResetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@WithMockUser
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatabaseResetService databaseResetService;

    @Test
    @DisplayName("POST /api/admin/reset-database - Should reset database successfully")
    void resetDatabase_ShouldReturnSuccess() throws Exception {
        Map<String, Integer> resetResult = new HashMap<>();
        resetResult.put("productsLoaded", 15);

        when(databaseResetService.resetDatabase()).thenReturn(resetResult);

        mockMvc.perform(post("/api/admin/reset-database")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Database reset successfully")))
                .andExpect(jsonPath("$.productsLoaded", is(15)));

        verify(databaseResetService, times(1)).resetDatabase();
    }

    @Test
    @DisplayName("POST /api/admin/reset-database - Should handle exception")
    void resetDatabase_WhenException_ShouldReturnError() throws Exception {
        when(databaseResetService.resetDatabase()).thenThrow(new RuntimeException("Database connection error"));

        mockMvc.perform(post("/api/admin/reset-database")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Error resetting database")));

        verify(databaseResetService, times(1)).resetDatabase();
    }

    @Test
    @DisplayName("POST /api/admin/reset-database - Should reset database with zero products")
    void resetDatabase_WithZeroProducts_ShouldReturnSuccess() throws Exception {
        Map<String, Integer> resetResult = new HashMap<>();
        resetResult.put("productsLoaded", 0);

        when(databaseResetService.resetDatabase()).thenReturn(resetResult);

        mockMvc.perform(post("/api/admin/reset-database")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.productsLoaded", is(0)));

        verify(databaseResetService, times(1)).resetDatabase();
    }
}
