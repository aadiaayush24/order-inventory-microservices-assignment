package com.inventory.service;

import com.inventory.dto.InventoryBatchResponse;
import com.inventory.dto.InventoryUpdateRequest;
import com.inventory.dto.InventoryUpdateResponse;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.factory.InventoryStrategyFactory;
import com.inventory.model.InventoryBatch;
import com.inventory.model.Product;
import com.inventory.repository.InventoryBatchRepository;
import com.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryStrategyFactory strategyFactory;

    /**
     * Get all inventory batches for a product, sorted by expiry date.
     *
     * @param productId The product identifier
     * @return List of inventory batch responses
     */
    @Transactional(readOnly = true)
    public List<InventoryBatchResponse> getInventoryByProductId(String productId) {
        log.info("Fetching inventory for product: {}", productId);
        
        if (!productRepository.existsByProductId(productId)) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        }
        
        List<InventoryBatch> batches = inventoryBatchRepository
                .findByProductIdOrderByExpiryDate(productId);
        
        log.info("Found {} batches for product {}", batches.size(), productId);
        
        return batches.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update inventory by deducting quantity for an order.
     * Uses the Factory Pattern to apply the configured inventory strategy.
     *
     * @param request The inventory update request
     * @param strategyType The strategy to use (FIFO, LIFO, etc.)
     * @return Response containing deduction details
     */
    @Transactional
    public InventoryUpdateResponse updateInventory(InventoryUpdateRequest request, String strategyType) {
        log.info("Updating inventory for product {} with quantity {} using strategy {}", 
                request.getProductId(), request.getQuantity(), strategyType);
        
        Product product = productRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with ID: " + request.getProductId()));
        
        // Get available batches (non-expired, with quantity > 0)
        List<InventoryBatch> availableBatches = inventoryBatchRepository
                .findAvailableBatchesByProductId(request.getProductId());
        
        if (availableBatches.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No available inventory batches for product: " + request.getProductId());
        }
        
        // Use factory to get the appropriate strategy
        var strategy = strategyFactory.getStrategy(strategyType);
        
        // Apply the strategy to deduct inventory
        InventoryUpdateResponse response = strategy.deductInventory(
                availableBatches, request.getQuantity());
        
        // Save the updated batches
        inventoryBatchRepository.saveAll(availableBatches);
        
        response.setProductId(request.getProductId());
        
        log.info("Successfully updated inventory for product {}", request.getProductId());
        return response;
    }

    /**
     * Update inventory using default FIFO strategy.
     *
     * @param request The inventory update request
     * @return Response containing deduction details
     */
    @Transactional
    public InventoryUpdateResponse updateInventory(InventoryUpdateRequest request) {
        return updateInventory(request, "FIFO");
    }

    private InventoryBatchResponse convertToResponse(InventoryBatch batch) {
        return InventoryBatchResponse.builder()
                .batchNumber(batch.getBatchNumber())
                .productId(batch.getProduct().getProductId())
                .productName(batch.getProduct().getName())
                .quantity(batch.getQuantity())
                .expiryDate(batch.getExpiryDate())
                .manufacturingDate(batch.getManufacturingDate())
                .isExpired(batch.isExpired())
                .build();
    }
}

