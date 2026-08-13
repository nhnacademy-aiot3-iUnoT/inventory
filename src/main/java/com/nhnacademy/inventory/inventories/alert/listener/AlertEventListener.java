package com.nhnacademy.inventory.inventories.alert.listener;

import com.nhnacademy.inventory.inventories.alert.event.StockOutboundCompletedEvent;
import com.nhnacademy.inventory.inventories.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEventListener {

    private final AlertService alertService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockOutbound(StockOutboundCompletedEvent event){
        try{
            log.info("재고 부족 알림 생성 - zoneId: {}, medicinePackageUnitId: {}",
                    event.zoneId(), event.medicinePackageUnitId());
            alertService.createLowStockAlert(event.zoneId(), event.medicinePackageUnitId());
        }catch (Exception e){
            log.error("재고 부족 알림 생성 실패 - {}", e.getMessage());
        }
    }
}
