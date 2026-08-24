package com.nhnacademy.inventory.inventories.transaction.repository;

import com.nhnacademy.inventory.global.config.QuerydslConfig;
import com.nhnacademy.inventory.inventories.transaction.domain.StockTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(QuerydslConfig.class)
class StockTransactionRepositoryImplTest {


    @Test
    void searchByCondition() {
    }
}