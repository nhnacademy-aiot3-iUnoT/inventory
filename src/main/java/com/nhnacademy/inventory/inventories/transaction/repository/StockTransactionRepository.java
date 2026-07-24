package com.nhnacademy.inventory.inventories.transaction.repository;

import com.nhnacademy.inventory.inventories.transaction.domain.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long>, StockTransactionRepositoryCustom {
}
