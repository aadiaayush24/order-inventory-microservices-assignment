package com.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO from Inventory Service update.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateResponse {
    private String productId;
    private Integer totalQuantityDeducted;
    private List<BatchDeduction> batchDeductions;
    private String message;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchDeduction {
        private String batchNumber;
        private Integer quantityDeducted;
    }
}

