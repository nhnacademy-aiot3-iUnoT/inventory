package com.nhnacademy.inventory.inventories.transaction.repository;

import com.nhnacademy.inventory.inventories.transaction.domain.StockTransaction;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StockTransactionRepository extends JpaRepository<StockTransaction, Long>, StockTransactionRepositoryCustom {

    // 임시: 추후 QueryDSL 적용
    // start는 포함, end는 미포함. 호출부에서 end에 마지막날 + 1일을 넘김.
    @Query("""
        SELECT st
        FROM StockTransaction st
        JOIN FETCH st.medicinePackageUnit mpu
        JOIN FETCH mpu.medicine
        WHERE st.zone.storage.organization.id = :organizationId
            AND st.transactionType IN :types
            AND st.processedAt >= :start
            AND st.processedAt < :end
    """)
    List<StockTransaction> findAllForReport(@Param("organizationId") Long organizationId,
                                             @Param("types") Collection<TransactionType> types,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}
