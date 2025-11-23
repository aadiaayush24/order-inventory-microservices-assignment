package com.inventory.factory;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory class for creating inventory management strategies.
 * This implements the Factory Design Pattern to allow extensible inventory management.
 * New strategies can be added without modifying existing code.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryStrategyFactory {

    private final List<InventoryStrategy> strategies;
    private final Map<String, InventoryStrategy> strategyMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // Register all available strategies
        for (InventoryStrategy strategy : strategies) {
            strategyMap.put(strategy.getStrategyType().toUpperCase(), strategy);
            log.info("Registered inventory strategy: {}", strategy.getStrategyType());
        }
    }

    /**
     * Get inventory strategy by type.
     * 
     * @param strategyType The type of strategy (FIFO, LIFO, etc.)
     * @return The inventory strategy implementation
     */
    public InventoryStrategy getStrategy(String strategyType) {
        String normalizedType = strategyType != null ? strategyType.toUpperCase() : "FIFO";
        InventoryStrategy strategy = strategyMap.get(normalizedType);
        
        if (strategy == null) {
            log.warn("Strategy '{}' not found, using FIFO as default", strategyType);
            return strategyMap.get("FIFO");
        }
        
        return strategy;
    }

    /**
     * Get the default strategy (FIFO).
     * 
     * @return The default inventory strategy
     */
    public InventoryStrategy getDefaultStrategy() {
        return getStrategy("FIFO");
    }
}

