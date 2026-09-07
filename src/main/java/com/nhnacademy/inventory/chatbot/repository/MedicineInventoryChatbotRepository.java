package com.nhnacademy.inventory.chatbot.repository;

import com.nhnacademy.inventory.chatbot.dto.ExpiringInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.LowStockInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.MedicinePackageUnitTargetRow;
import com.nhnacademy.inventory.chatbot.dto.MedicineInventorySearchRow;
import com.nhnacademy.inventory.chatbot.dto.ZoneTargetRow;
import com.nhnacademy.inventory.chatbot.dto.query.FindExpiringInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindLowStockInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindMedicinePackageUnitTargetQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindZoneTargetQuery;
import com.nhnacademy.inventory.chatbot.dto.query.SearchMedicineInventoryQuery;
import java.util.List;
import java.util.Optional;

public interface MedicineInventoryChatbotRepository {
    // 재고 및 위치 조회
    List<MedicineInventorySearchRow> search(SearchMedicineInventoryQuery query);

    // 유통기한 임박 재고 조회
    List<ExpiringInventoryRow> findExpiring(FindExpiringInventoryQuery query);

    // 최소 재고 이하 조회
    List<LowStockInventoryRow> findLowStock(FindLowStockInventoryQuery query);

    // 입출고 대상 의약품 포장 단위 조회
    List<MedicinePackageUnitTargetRow> findPackageUnitTargets(FindMedicinePackageUnitTargetQuery query);

    // ID로 입고 대상 의약품 포장 단위 조회
    Optional<MedicinePackageUnitTargetRow> findPackageUnitTargetById(Long packageUnitId);

    // 입출고 대상 저장소 구역 조회
    List<ZoneTargetRow> findZoneTargets(FindZoneTargetQuery query);

    // 접근 가능한 저장소 범위에서 ID로 입고 대상 구역 조회
    Optional<ZoneTargetRow> findZoneTargetById(Long zoneId, List<Long> storageIds);
}
