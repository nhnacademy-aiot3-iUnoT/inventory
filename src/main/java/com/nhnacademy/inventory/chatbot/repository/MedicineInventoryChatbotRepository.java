package com.nhnacademy.inventory.chatbot.repository;

import com.nhnacademy.inventory.chatbot.dto.ExpiringInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.LowStockInventoryRow;
import com.nhnacademy.inventory.chatbot.dto.MedicineInventorySearchRow;
import com.nhnacademy.inventory.chatbot.dto.query.FindExpiringInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindLowStockInventoryQuery;
import com.nhnacademy.inventory.chatbot.dto.query.SearchMedicineInventoryQuery;
import java.util.List;

public interface MedicineInventoryChatbotRepository {
    // 재고 및 위치 조회
    List<MedicineInventorySearchRow> search(SearchMedicineInventoryQuery query);

    // 유통기한 임박 재고 조회
    List<ExpiringInventoryRow> findExpiring(FindExpiringInventoryQuery query);

    // 최소 재고 이하 조회
    List<LowStockInventoryRow> findLowStock(FindLowStockInventoryQuery query);
}
