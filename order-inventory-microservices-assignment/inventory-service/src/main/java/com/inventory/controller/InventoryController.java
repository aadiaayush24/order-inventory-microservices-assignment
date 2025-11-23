package com.inventory.controller;

import com.inventory.dto.InventoryBatchResponse;
import com.inventory.dto.InventoryUpdateRequest;
import com.inventory.dto.InventoryUpdateResponse;
import com.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "APIs for managing product inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    @Operation(summary = "Get inventory batches by product ID",
               description = "Returns all inventory batches for a product, sorted by expiry date (earliest first)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved inventory"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<List<InventoryBatchResponse>> getInventory(
            @Parameter(description = "Product identifier", required = true)
            @PathVariable String productId) {
        
        List<InventoryBatchResponse> inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(inventory);
    }

    @PostMapping("/update")
    @Operation(summary = "Update inventory after order",
               description = "Deducts inventory quantity for an order. Uses FIFO strategy by default.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventory updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or insufficient inventory"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<InventoryUpdateResponse> updateInventory(
            @Parameter(description = "Inventory update request", required = true)
            @Valid @RequestBody InventoryUpdateRequest request,
            @Parameter(description = "Inventory deduction strategy (FIFO, LIFO)")
            @RequestParam(required = false, defaultValue = "FIFO") String strategy) {
        
        InventoryUpdateResponse response = inventoryService.updateInventory(request, strategy);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the service is running")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Inventory Service is running");
    }
}

