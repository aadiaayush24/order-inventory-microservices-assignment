package com.inventory.factory;

import com.inventory.dto.InventoryUpdateResponse;
import com.inventory.model.InventoryBatch;

import java.util.List;

/**
 * Strategy interface for different inventory management approaches.
 * This allows for extensibility in how inventory is deducted/managed.
 */
public interface InventoryStrategy {
    
    /**
     * Deduct inventory using a specific strategy.
     * 
     * @param batches Available inventory batches
     * @param quantity Quantity to deduct
     * @return Response containing deduction details
     */
    InventoryUpdateResponse deductInventory(List<InventoryBatch> batches, int quantity);
    
    /**
     * Get the strategy type identifier.
     * 
     * @return Strategy type name
     */
    String getStrategyType();
}

