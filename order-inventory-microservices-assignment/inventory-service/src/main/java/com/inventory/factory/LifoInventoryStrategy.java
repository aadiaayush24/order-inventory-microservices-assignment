package com.inventory.factory;

import com.inventory.dto.InventoryUpdateResponse;
import com.inventory.exception.InsufficientInventoryException;
import com.inventory.model.InventoryBatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LIFO (Last In First Out) strategy - deducts from latest expiry date first.
 * This strategy can be used for products where newer stock should be used first.
 */
@Component
@Slf4j
public class LifoInventoryStrategy implements InventoryStrategy {

    @Override
    public InventoryUpdateResponse deductInventory(List<InventoryBatch> batches, int quantity) {
        log.info("Applying LIFO strategy to deduct {} units", quantity);
        
        // Reverse the list to process from latest expiry date first
        List<InventoryBatch> reversedBatches = new ArrayList<>(batches);
        Collections.reverse(reversedBatches);
        
        int remainingQuantity = quantity;
        List<InventoryUpdateResponse.BatchDeduction> deductions = new ArrayList<>();
        
        for (InventoryBatch batch : reversedBatches) {
            if (remainingQuantity <= 0) {
                break;
            }
            
            int availableInBatch = batch.getQuantity();
            int toDeduct = Math.min(remainingQuantity, availableInBatch);
            
            batch.reduceQuantity(toDeduct);
            remainingQuantity -= toDeduct;
            
            deductions.add(InventoryUpdateResponse.BatchDeduction.builder()
                    .batchNumber(batch.getBatchNumber())
                    .quantityDeducted(toDeduct)
                    .build());
            
            log.debug("Deducted {} units from batch {}", toDeduct, batch.getBatchNumber());
        }
        
        if (remainingQuantity > 0) {
            throw new InsufficientInventoryException(
                "Insufficient inventory. Required: " + quantity + ", Available: " + (quantity - remainingQuantity)
            );
        }
        
        return InventoryUpdateResponse.builder()
                .totalQuantityDeducted(quantity)
                .batchDeductions(deductions)
                .message("Inventory deducted successfully using LIFO strategy")
                .build();
    }

    @Override
    public String getStrategyType() {
        return "LIFO";
    }
}

