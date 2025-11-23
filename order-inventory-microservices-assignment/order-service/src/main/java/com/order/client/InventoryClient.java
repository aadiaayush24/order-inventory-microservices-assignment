package com.order.client;

import com.order.dto.InventoryUpdateRequest;
import com.order.dto.InventoryUpdateResponse;
import com.order.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Client for communicating with the Inventory Service.
 * Uses WebClient for non-blocking HTTP communication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    /**
     * Update inventory for an order.
     * 
     * @param request The inventory update request
     * @return The inventory update response
     */
    public InventoryUpdateResponse updateInventory(InventoryUpdateRequest request) {
        log.info("Calling Inventory Service to update inventory for product: {}", request.getProductId());
        
        try {
            return webClientBuilder.build()
                    .post()
                    .uri(inventoryServiceUrl + "/inventory/update")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatus.NOT_FOUND::equals, 
                            response -> Mono.error(new InventoryServiceException("Product not found in inventory")))
                    .onStatus(HttpStatus.BAD_REQUEST::equals,
                            response -> response.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new InventoryServiceException("Insufficient inventory: " + body))))
                    .onStatus(x -> x.isError(),
                            response -> Mono.error(new InventoryServiceException("Inventory service error")))
                    .bodyToMono(InventoryUpdateResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e) {
            log.error("Error calling Inventory Service: {}", e.getMessage(), e);
            throw new InventoryServiceException("Failed to communicate with Inventory Service: " + e.getMessage());
        }
    }

    /**
     * Check inventory service health.
     * 
     * @return true if service is available, false otherwise
     */
    public boolean checkHealth() {
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri(inventoryServiceUrl + "/inventory/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
            
            return response != null && response.contains("running");
        } catch (Exception e) {
            log.warn("Inventory Service health check failed: {}", e.getMessage());
            return false;
        }
    }
}

