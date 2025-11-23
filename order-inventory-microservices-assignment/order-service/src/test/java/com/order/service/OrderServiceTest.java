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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Order Service Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest testOrderRequest;
    private Order testOrder;
    private InventoryUpdateResponse inventoryResponse;

    @BeforeEach
    void setUp() {
        testOrderRequest = OrderRequest.builder()
                .productId("PROD-001")
                .quantity(10)
                .customerName("John Doe")
                .customerEmail("john.doe@example.com")
                .build();

        testOrder = Order.builder()
                .id(1L)
                .orderId("ORD-12345678")
                .productId("PROD-001")
                .quantity(10)
                .customerName("John Doe")
                .customerEmail("john.doe@example.com")
                .status(OrderStatus.PENDING)
                .build();

        inventoryResponse = InventoryUpdateResponse.builder()
                .productId("PROD-001")
                .totalQuantityDeducted(10)
                .batchDeductions(Collections.emptyList())
                .message("Inventory updated successfully")
                .build();
    }

    @Test
    @DisplayName("Should place order successfully")
    void shouldPlaceOrderSuccessfully() {
        // Given
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class)))
                .thenReturn(inventoryResponse);

        // When
        OrderResponse response = orderService.placeOrder(testOrderRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).startsWith("ORD-");
        assertThat(response.getProductId()).isEqualTo("PROD-001");
        assertThat(response.getQuantity()).isEqualTo(10);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(2)).save(orderCaptor.capture());
        verify(inventoryClient).updateInventory(any(InventoryUpdateRequest.class));
    }

    @Test
    @DisplayName("Should mark order as failed when inventory update fails")
    void shouldMarkOrderAsFailedWhenInventoryUpdateFails() {
        // Given
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(inventoryClient.updateInventory(any(InventoryUpdateRequest.class)))
                .thenThrow(new InventoryServiceException("Insufficient inventory"));

        // When & Then
        assertThatThrownBy(() -> orderService.placeOrder(testOrderRequest))
                .isInstanceOf(InventoryServiceException.class)
                .hasMessageContaining("Insufficient inventory");

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(2)).save(orderCaptor.capture());
        
        Order savedOrder = orderCaptor.getAllValues().get(1);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.FAILED);
        assertThat(savedOrder.getFailureReason()).contains("Insufficient inventory");
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void shouldGetOrderByIdSuccessfully() {
        // Given
        when(orderRepository.findByOrderId("ORD-12345678"))
                .thenReturn(Optional.of(testOrder));

        // When
        OrderResponse response = orderService.getOrder("ORD-12345678");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo("ORD-12345678");
        assertThat(response.getProductId()).isEqualTo("PROD-001");
        verify(orderRepository).findByOrderId("ORD-12345678");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order not found")
    void shouldThrowExceptionWhenOrderNotFound() {
        // Given
        when(orderRepository.findByOrderId("INVALID")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrder("INVALID"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found");
    }

    @Test
    @DisplayName("Should cancel pending order successfully")
    void shouldCancelPendingOrderSuccessfully() {
        // Given
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findByOrderId("ORD-12345678"))
                .thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        OrderResponse response = orderService.cancelOrder("ORD-12345678");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling confirmed order")
    void shouldThrowExceptionWhenCancellingConfirmedOrder() {
        // Given
        testOrder.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByOrderId("ORD-12345678"))
                .thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder("ORD-12345678"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel confirmed order");
    }

    @Test
    @DisplayName("Should throw exception when cancelling already cancelled order")
    void shouldThrowExceptionWhenCancellingAlreadyCancelledOrder() {
        // Given
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findByOrderId("ORD-12345678"))
                .thenReturn(Optional.of(testOrder));

        // When & Then
        assertThatThrownBy(() -> orderService.cancelOrder("ORD-12345678"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already cancelled");
    }
}

