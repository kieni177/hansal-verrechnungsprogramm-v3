package com.hansal.verrechnungsprogramm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hansal.verrechnungsprogramm.dto.MeatCutAvailabilityDTO;
import com.hansal.verrechnungsprogramm.model.MeatCut;
import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.service.MeatCutService;
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
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeatCutController.class)
@WithMockUser
class MeatCutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MeatCutService meatCutService;

    private MeatCut testMeatCut;
    private Product testProduct;
    private List<MeatCut> testMeatCuts;
    private MeatCutAvailabilityDTO testAvailabilityDTO;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Rindfleisch");
        testProduct.setPrice(new BigDecimal("25.00"));

        testMeatCut = new MeatCut();
        testMeatCut.setId(1L);
        testMeatCut.setProduct(testProduct);
        testMeatCut.setTotalWeight(new BigDecimal("50.00"));
        testMeatCut.setAvailableWeight(new BigDecimal("30.00"));
        testMeatCut.setPricePerKg(new BigDecimal("25.00"));

        MeatCut meatCut2 = new MeatCut();
        meatCut2.setId(2L);
        meatCut2.setProduct(testProduct);
        meatCut2.setTotalWeight(new BigDecimal("40.00"));
        meatCut2.setAvailableWeight(new BigDecimal("40.00"));
        meatCut2.setPricePerKg(new BigDecimal("25.00"));

        testMeatCuts = Arrays.asList(testMeatCut, meatCut2);

        testAvailabilityDTO = new MeatCutAvailabilityDTO();
        testAvailabilityDTO.setMeatCutId(1L);
        testAvailabilityDTO.setCowTag("AT-1234567");
        testAvailabilityDTO.setCowId("COW-001");
        testAvailabilityDTO.setSlaughterDate(LocalDate.of(2024, 1, 15));
        testAvailabilityDTO.setAvailableWeight(new BigDecimal("30.00"));
        testAvailabilityDTO.setTotalWeight(new BigDecimal("50.00"));
        testAvailabilityDTO.setPricePerKg(new BigDecimal("25.00"));
        testAvailabilityDTO.setProductName("Rindfleisch");
    }

    @Test
    @DisplayName("GET /api/meat-cuts - Should return all meat cuts")
    void getAllMeatCuts_ShouldReturnAllMeatCuts() throws Exception {
        when(meatCutService.getAllMeatCuts()).thenReturn(testMeatCuts);

        mockMvc.perform(get("/api/meat-cuts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));

        verify(meatCutService, times(1)).getAllMeatCuts();
    }

    @Test
    @DisplayName("GET /api/meat-cuts/{id} - Should return meat cut by ID")
    void getMeatCutById_ShouldReturnMeatCut() throws Exception {
        when(meatCutService.getMeatCutById(1L)).thenReturn(testMeatCut);

        mockMvc.perform(get("/api/meat-cuts/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.totalWeight", is(50.00)))
                .andExpect(jsonPath("$.availableWeight", is(30.00)));

        verify(meatCutService, times(1)).getMeatCutById(1L);
    }

    @Test
    @DisplayName("GET /api/meat-cuts/available - Should return available meat cuts")
    void getAvailableMeatCuts_ShouldReturnAvailableMeatCuts() throws Exception {
        when(meatCutService.getAvailableMeatCuts()).thenReturn(testMeatCuts);

        mockMvc.perform(get("/api/meat-cuts/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(meatCutService, times(1)).getAvailableMeatCuts();
    }

    @Test
    @DisplayName("GET /api/meat-cuts/slaughter/{slaughterId} - Should return meat cuts by slaughter")
    void getMeatCutsBySlaughter_ShouldReturnMeatCuts() throws Exception {
        when(meatCutService.getMeatCutsBySlaughter(1L)).thenReturn(testMeatCuts);

        mockMvc.perform(get("/api/meat-cuts/slaughter/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(meatCutService, times(1)).getMeatCutsBySlaughter(1L);
    }

    @Test
    @DisplayName("GET /api/meat-cuts/search - Should search meat cuts by product and weight")
    void searchMeatCuts_ShouldReturnMatchingMeatCuts() throws Exception {
        when(meatCutService.searchMeatCuts(1L, new BigDecimal("20.00"))).thenReturn(List.of(testMeatCut));

        mockMvc.perform(get("/api/meat-cuts/search")
                        .param("productId", "1")
                        .param("minWeight", "20.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].availableWeight", is(30.00)));

        verify(meatCutService, times(1)).searchMeatCuts(1L, new BigDecimal("20.00"));
    }

    @Test
    @DisplayName("GET /api/meat-cuts/availability/product/{productId} - Should return availability by product")
    void getAvailabilityByProduct_ShouldReturnAvailability() throws Exception {
        when(meatCutService.getAvailabilityByProduct(1L)).thenReturn(List.of(testAvailabilityDTO));

        mockMvc.perform(get("/api/meat-cuts/availability/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].meatCutId", is(1)))
                .andExpect(jsonPath("$[0].cowTag", is("AT-1234567")))
                .andExpect(jsonPath("$[0].productName", is("Rindfleisch")));

        verify(meatCutService, times(1)).getAvailabilityByProduct(1L);
    }

    @Test
    @DisplayName("GET /api/meat-cuts - Should return empty list when no meat cuts")
    void getAllMeatCuts_WhenNoMeatCuts_ShouldReturnEmptyList() throws Exception {
        when(meatCutService.getAllMeatCuts()).thenReturn(List.of());

        mockMvc.perform(get("/api/meat-cuts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(meatCutService, times(1)).getAllMeatCuts();
    }

    @Test
    @DisplayName("GET /api/meat-cuts/available - Should return empty list when no available meat cuts")
    void getAvailableMeatCuts_WhenNoneAvailable_ShouldReturnEmptyList() throws Exception {
        when(meatCutService.getAvailableMeatCuts()).thenReturn(List.of());

        mockMvc.perform(get("/api/meat-cuts/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(meatCutService, times(1)).getAvailableMeatCuts();
    }

    @Test
    @DisplayName("GET /api/meat-cuts/search - Should return empty list when no match")
    void searchMeatCuts_WhenNoMatch_ShouldReturnEmptyList() throws Exception {
        when(meatCutService.searchMeatCuts(999L, new BigDecimal("100.00"))).thenReturn(List.of());

        mockMvc.perform(get("/api/meat-cuts/search")
                        .param("productId", "999")
                        .param("minWeight", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(meatCutService, times(1)).searchMeatCuts(999L, new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("GET /api/meat-cuts/availability/product/{productId} - Should return empty when no availability")
    void getAvailabilityByProduct_WhenNoneAvailable_ShouldReturnEmptyList() throws Exception {
        when(meatCutService.getAvailabilityByProduct(999L)).thenReturn(List.of());

        mockMvc.perform(get("/api/meat-cuts/availability/product/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(meatCutService, times(1)).getAvailabilityByProduct(999L);
    }
}
