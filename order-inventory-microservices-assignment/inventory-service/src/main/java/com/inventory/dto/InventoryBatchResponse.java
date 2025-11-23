package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBatchResponse {
    
    private String batchNumber;
    private String productId;
    private String productName;
    private Integer quantity;
    private LocalDate expiryDate;
    private LocalDate manufacturingDate;
    private Boolean isExpired;
}

