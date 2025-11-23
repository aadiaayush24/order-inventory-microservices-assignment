package com.inventory.factory;

import com.inventory.dto.InventoryUpdateResponse;
import com.inventory.exception.InsufficientInventoryException;
import com.inventory.model.InventoryBatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * FIFO (First In First Out) strategy - deducts from earliest expiry date first.
 * This is the default strategy for inventory management.
 */
@Component
@Slf4j
public class FifoInventoryStrategy implements InventoryStrategy {

    @Override
    public InventoryUpdateResponse deductInventory(List<InventoryBatch> batches, int quantity) {
        log.info("Applying FIFO strategy to deduct {} units", quantity);
        
        int remainingQuantity = quantity;
        List<InventoryUpdateResponse.BatchDeduction> deductions = new ArrayList<>();
        
        for (InventoryBatch batch : batches) {
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
                .message("Inventory deducted successfully using FIFO strategy")
                .build();
    }

    @Override
    public String getStrategyType() {
        return "FIFO";
    }
}

