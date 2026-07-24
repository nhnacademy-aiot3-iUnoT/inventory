package com.nhnacademy.inventory.inventories.alert.repository;

import com.nhnacademy.inventory.inventories.alert.domain.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {
}
