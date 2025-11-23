package com.inventory.service;

import com.inventory.dto.InventoryBatchResponse;
import com.inventory.dto.InventoryUpdateRequest;
import com.inventory.dto.InventoryUpdateResponse;
import com.inventory.exception.InsufficientInventoryException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.factory.FifoInventoryStrategy;
import com.inventory.factory.InventoryStrategyFactory;
import com.inventory.model.InventoryBatch;
import com.inventory.model.Product;
import com.inventory.repository.InventoryBatchRepository;
import com.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Inventory Service Unit Tests")
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryBatchRepository inventoryBatchRepository;

    @Mock
    private InventoryStrategyFactory strategyFactory;

    @InjectMocks
    private InventoryService inventoryService;

    private Product testProduct;
    private List<InventoryBatch> testBatches;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .productId("PROD-001")
                .name("Test Product")
                .description("Test Description")
                .build();

        InventoryBatch batch1 = InventoryBatch.builder()
                .id(1L)
                .batchNumber("BATCH-001")
                .product(testProduct)
                .quantity(50)
                .expiryDate(LocalDate.now().plusMonths(6))
                .manufacturingDate(LocalDate.now().minusMonths(1))
                .build();

        InventoryBatch batch2 = InventoryBatch.builder()
                .id(2L)
                .batchNumber("BATCH-002")
                .product(testProduct)
                .quantity(30)
                .expiryDate(LocalDate.now().plusMonths(12))
                .manufacturingDate(LocalDate.now().minusMonths(1))
                .build();

        testBatches = Arrays.asList(batch1, batch2);
    }

    @Test
    @DisplayName("Should get inventory batches by product ID successfully")
    void shouldGetInventoryByProductId() {
        // Given
        when(productRepository.existsByProductId("PROD-001")).thenReturn(true);
        when(inventoryBatchRepository.findByProductIdOrderByExpiryDate("PROD-001"))
                .thenReturn(testBatches);

        // When
        List<InventoryBatchResponse> result = inventoryService.getInventoryByProductId("PROD-001");

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getBatchNumber()).isEqualTo("BATCH-001");
        assertThat(result.get(0).getQuantity()).isEqualTo(50);
        verify(productRepository).existsByProductId("PROD-001");
        verify(inventoryBatchRepository).findByProductIdOrderByExpiryDate("PROD-001");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product does not exist")
    void shouldThrowExceptionWhenProductNotFound() {
        // Given
        when(productRepository.existsByProductId("INVALID")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> inventoryService.getInventoryByProductId("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository).existsByProductId("INVALID");
        verify(inventoryBatchRepository, never()).findByProductIdOrderByExpiryDate(anyString());
    }

    @Test
    @DisplayName("Should update inventory successfully using FIFO strategy")
    void shouldUpdateInventorySuccessfully() {
        // Given
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .productId("PROD-001")
                .quantity(40)
                .build();

        FifoInventoryStrategy fifoStrategy = new FifoInventoryStrategy();
        
        when(productRepository.findByProductId("PROD-001")).thenReturn(Optional.of(testProduct));
        when(inventoryBatchRepository.findAvailableBatchesByProductId("PROD-001"))
                .thenReturn(testBatches);
        when(strategyFactory.getStrategy("FIFO")).thenReturn(fifoStrategy);
        when(inventoryBatchRepository.saveAll(anyList())).thenReturn(testBatches);

        // When
        InventoryUpdateResponse response = inventoryService.updateInventory(request, "FIFO");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo("PROD-001");
        assertThat(response.getTotalQuantityDeducted()).isEqualTo(40);
        verify(inventoryBatchRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found for update")
    void shouldThrowExceptionWhenProductNotFoundForUpdate() {
        // Given
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .productId("INVALID")
                .quantity(10)
                .build();

        when(productRepository.findByProductId("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inventoryService.updateInventory(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository).findByProductId("INVALID");
        verify(inventoryBatchRepository, never()).findAvailableBatchesByProductId(anyString());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no available batches")
    void shouldThrowExceptionWhenNoAvailableBatches() {
        // Given
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .productId("PROD-001")
                .quantity(10)
                .build();

        when(productRepository.findByProductId("PROD-001")).thenReturn(Optional.of(testProduct));
        when(inventoryBatchRepository.findAvailableBatchesByProductId("PROD-001"))
                .thenReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> inventoryService.updateInventory(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No available inventory batches");
    }
}

