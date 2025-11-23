package com.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.dto.InventoryUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Inventory Controller Integration Tests")
class InventoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should get inventory batches for existing product")
    void shouldGetInventoryBatchesSuccessfully() throws Exception {
        mockMvc.perform(get("/inventory/PROD-001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].productId").value("PROD-001"))
                .andExpect(jsonPath("$[0].batchNumber").exists())
                .andExpect(jsonPath("$[0].quantity").exists())
                .andExpect(jsonPath("$[0].expiryDate").exists());
    }

    @Test
    @DisplayName("Should return 404 for non-existent product")
    void shouldReturn404ForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/inventory/NON-EXISTENT"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(containsString("Product not found")));
    }

    @Test
    @DisplayName("Should update inventory successfully")
    void shouldUpdateInventorySuccessfully() throws Exception {
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .productId("PROD-001")
                .quantity(10)
                .build();

        mockMvc.perform(post("/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value("PROD-001"))
                .andExpect(jsonPath("$.totalQuantityDeducted").value(10))
                .andExpect(jsonPath("$.batchDeductions").isArray())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 400 for invalid inventory update request")
    void shouldReturn400ForInvalidRequest() throws Exception {
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .productId("")
                .quantity(0)
                .build();

        mockMvc.perform(post("/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for insufficient inventory")
    void shouldReturn400ForInsufficientInventory() throws Exception {
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .productId("PROD-001")
                .quantity(10000)
                .build();

        mockMvc.perform(post("/inventory/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient Inventory"));
    }

    @Test
    @DisplayName("Should return health check")
    void shouldReturnHealthCheck() throws Exception {
        mockMvc.perform(get("/inventory/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("running")));
    }
}

