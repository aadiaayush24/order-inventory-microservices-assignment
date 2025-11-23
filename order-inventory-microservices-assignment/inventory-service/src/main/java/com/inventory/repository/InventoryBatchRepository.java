package com.inventory.repository;

import com.inventory.model.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
    
    @Query("SELECT ib FROM InventoryBatch ib " +
           "WHERE ib.product.productId = :productId " +
           "ORDER BY ib.expiryDate ASC")
    List<InventoryBatch> findByProductIdOrderByExpiryDate(@Param("productId") String productId);
    
    Optional<InventoryBatch> findByBatchNumber(String batchNumber);
    
    @Query("SELECT ib FROM InventoryBatch ib " +
           "WHERE ib.product.productId = :productId " +
           "AND ib.quantity > 0 " +
           "AND ib.expiryDate > CURRENT_DATE " +
           "ORDER BY ib.expiryDate ASC")
    List<InventoryBatch> findAvailableBatchesByProductId(@Param("productId") String productId);
}

