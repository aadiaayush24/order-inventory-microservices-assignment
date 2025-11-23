package com.order.service;

import com.order.client.InventoryClient;
import com.order.dto.InventoryUpdateRequest;
import com.order.dto.InventoryUpdateResponse;
import com.order.dto.OrderRequest;
import com.order.dto.OrderResponse;
import com.order.exception.InventoryServiceException;
import com.order.exception.ResourceNotFoundException;
import com.order.model.Order;
import com.order.model.OrderStatus;
import com.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    /**
     * Place a new order and update inventory.
     * 
     * @param request The order request
     * @return Order response with status
     */
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        log.info("Processing order for product {} with quantity {}", 
                request.getProductId(), request.getQuantity());
        
        // Generate unique order ID
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        // Create order entity
        Order order = Order.builder()
                .orderId(orderId)
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .status(OrderStatus.PENDING)
                .build();
        
        try {
            // Save order with pending status
            order = orderRepository.save(order);
            log.info("Order {} created with PENDING status", orderId);
            
            // Call Inventory Service to update inventory
            InventoryUpdateRequest inventoryRequest = InventoryUpdateRequest.builder()
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .build();
            
            InventoryUpdateResponse inventoryResponse = inventoryClient.updateInventory(inventoryRequest);
            log.info("Inventory updated successfully for order {}", orderId);
            
            // Update order status to confirmed
            order.setStatus(OrderStatus.CONFIRMED);
            order = orderRepository.save(order);
            
            return convertToResponse(order, "Order placed successfully");
            
        } catch (InventoryServiceException e) {
            log.error("Failed to update inventory for order {}: {}", orderId, e.getMessage());
            
            // Update order status to failed
            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason(e.getMessage());
            orderRepository.save(order);
            
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while processing order {}: {}", orderId, e.getMessage(), e);
            
            // Update order status to failed
            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason("Unexpected error: " + e.getMessage());
            orderRepository.save(order);
            
            throw new RuntimeException("Failed to process order: " + e.getMessage());
        }
    }

    /**
     * Get order by order ID.
     * 
     * @param orderId The order identifier
     * @return Order response
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        log.info("Fetching order: {}", orderId);
        
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        return convertToResponse(order, null);
    }

    /**
     * Cancel an order (if not already confirmed).
     * 
     * @param orderId The order identifier
     * @return Order response
     */
    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        log.info("Cancelling order: {}", orderId);
        
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel confirmed order. Order ID: " + orderId);
        }
        
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled. Order ID: " + orderId);
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order = orderRepository.save(order);
        
        return convertToResponse(order, "Order cancelled successfully");
    }

    private OrderResponse convertToResponse(Order order, String message) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .failureReason(order.getFailureReason())
                .message(message)
                .build();
    }
}

