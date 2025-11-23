package com.inventory.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Inventory Strategy Factory Tests")
class InventoryStrategyFactoryTest {

    private InventoryStrategyFactory factory;
    private List<InventoryStrategy> strategies;

    @BeforeEach
    void setUp() {
        strategies = Arrays.asList(
                new FifoInventoryStrategy(),
                new LifoInventoryStrategy()
        );
        factory = new InventoryStrategyFactory(strategies);
        factory.init();
    }

    @Test
    @DisplayName("Should return FIFO strategy")
    void shouldReturnFifoStrategy() {
        // When
        InventoryStrategy strategy = factory.getStrategy("FIFO");

        // Then
        assertThat(strategy).isNotNull();
        assertThat(strategy.getStrategyType()).isEqualTo("FIFO");
    }

    @Test
    @DisplayName("Should return LIFO strategy")
    void shouldReturnLifoStrategy() {
        // When
        InventoryStrategy strategy = factory.getStrategy("LIFO");

        // Then
        assertThat(strategy).isNotNull();
        assertThat(strategy.getStrategyType()).isEqualTo("LIFO");
    }

    @Test
    @DisplayName("Should return default FIFO strategy for unknown type")
    void shouldReturnDefaultStrategyForUnknownType() {
        // When
        InventoryStrategy strategy = factory.getStrategy("UNKNOWN");

        // Then
        assertThat(strategy).isNotNull();
        assertThat(strategy.getStrategyType()).isEqualTo("FIFO");
    }

    @Test
    @DisplayName("Should return default strategy when called explicitly")
    void shouldReturnDefaultStrategy() {
        // When
        InventoryStrategy strategy = factory.getDefaultStrategy();

        // Then
        assertThat(strategy).isNotNull();
        assertThat(strategy.getStrategyType()).isEqualTo("FIFO");
    }

    @Test
    @DisplayName("Should be case insensitive for strategy type")
    void shouldBeCaseInsensitive() {
        // When
        InventoryStrategy strategy1 = factory.getStrategy("fifo");
        InventoryStrategy strategy2 = factory.getStrategy("FIFO");
        InventoryStrategy strategy3 = factory.getStrategy("Fifo");

        // Then
        assertThat(strategy1.getStrategyType()).isEqualTo("FIFO");
        assertThat(strategy2.getStrategyType()).isEqualTo("FIFO");
        assertThat(strategy3.getStrategyType()).isEqualTo("FIFO");
    }
}

