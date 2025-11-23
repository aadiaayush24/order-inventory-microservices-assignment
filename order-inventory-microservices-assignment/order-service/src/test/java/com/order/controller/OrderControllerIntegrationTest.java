package com.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.client.InventoryClient;
import com.order.dto.InventoryUpdateResponse;
import com.order.dto.OrderRequest;
import com.order.exception.InventoryServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Order Controller Integration Tests")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryClient inventoryClient;

    @Test
    @DisplayName("Should place order successfully")
    void shouldPlaceOrderSuccessfully() throws Exception {
        // Given
        OrderRequest request = OrderRequest.builder()
                .productId("PROD-001")
                .quantity(5)
                .customerName("Jane Doe")
                .customerEmail("jane.doe@example.com")
                .build();

        InventoryUpdateResponse inventoryResponse = InventoryUpdateResponse.builder()
                .productId("PROD-001")
                .totalQuantityDeducted(5)
                .batchDeductions(Collections.emptyList())
                .message("Success")
                .build();

        when(inventoryClient.updateInventory(any())).thenReturn(inventoryResponse);

        // When & Then
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(startsWith("ORD-")))
                .andExpect(jsonPath("$.productId").value("PROD-001"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.customerName").value("Jane Doe"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.message").exists());

        verify(inventoryClient, times(1)).updateInventory(any());
    }

    @Test
    @DisplayName("Should return 503 when inventory service fails")
    void shouldReturn503WhenInventoryServiceFails() throws Exception {
        // Given
        OrderRequest request = OrderRequest.builder()
                .productId("PROD-001")
                .quantity(1000)
                .customerName("Jane Doe")
                .customerEmail("jane.doe@example.com")
                .build();

        when(inventoryClient.updateInventory(any()))
                .thenThrow(new InventoryServiceException("Insufficient inventory"));

        // When & Then
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("Inventory Service Error"));
    }

    @Test
    @DisplayName("Should return 400 for invalid order request")
    void shouldReturn400ForInvalidRequest() throws Exception {
        // Given
        OrderRequest request = OrderRequest.builder()
                .productId("")
                .quantity(0)
                .customerName("")
                .build();

        // When & Then
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(inventoryClient, never()).updateInventory(any());
    }

    @Test
    @DisplayName("Should return health check")
    void shouldReturnHealthCheck() throws Exception {
        mockMvc.perform(get("/order/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("running")));
    }
}

