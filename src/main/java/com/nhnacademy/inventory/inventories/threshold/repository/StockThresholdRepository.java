package com.nhnacademy.inventory.inventories.threshold.repository;

import com.nhnacademy.inventory.inventories.threshold.domain.StockThreshold;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockThresholdRepository extends JpaRepository<StockThreshold, Long> {
}
