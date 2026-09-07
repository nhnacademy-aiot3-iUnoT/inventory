package com.nhnacademy.inventory.chatbot.service;

import com.nhnacademy.inventory.chatbot.dto.ConsumptionSummaryRow;
import com.nhnacademy.inventory.chatbot.dto.StockPositionRow;
import com.nhnacademy.inventory.chatbot.dto.response.ReorderSuggestionResponse;
import com.nhnacademy.inventory.chatbot.dto.response.ReorderSuggestionResponse.Item;
import com.nhnacademy.inventory.chatbot.repository.StockTransactionChatbotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReorderSuggestionService {

    private static final int DEFAULT_WEEKS = 4;
    private static final int EXPIRY_WINDOW_DAYS = 7;
    private static final int MAX_RESULTS = 10;
    private static final int DAYS_PER_WEEK = 7;
    private static final double TREND_MIN = 0.5;
    private static final double TREND_MAX = 2.0;
    private static final double MAX_DISPOSAL_ADJUSTMENT = 0.5;
    private final StockTransactionChatbotRepository repository;


    private record Key(Long packageUnitId, Long storageId) {
    }

    public ReorderSuggestionResponse analyze(List<Long> storageIds, Integer analysisWeeks) {
        int weeks = analysisWeeks == null ? DEFAULT_WEEKS : analysisWeeks;

        if (storageIds.isEmpty()) {
            return ReorderSuggestionResponse.empty(weeks);
        }

        LocalDateTime now = LocalDateTime.now();

        Map<Key, Long> outbound = toQuantityMap(repository.sumOutboundTransactions(storageIds, now.minusWeeks(weeks), now)); // 설정된 주간까지 출고량
        Map<Key, Long> disposal = toQuantityMap(repository.sumDisposalTransactions(storageIds, now.minusWeeks(weeks), now)); // 설정된 주간까지 폐기량
        Map<Key, Long> lastWeekOutbound = toQuantityMap(repository.sumOutboundTransactions(storageIds, now.minusWeeks(1), now)); // 지난주 출고량

        List<StockPositionRow> positions = repository.findStockPositions(storageIds, LocalDate.now().plusDays(EXPIRY_WINDOW_DAYS));

        List<Item> items = positions.stream()
                .map(position -> {
                    Key key = new Key(position.packageUnitId(), position.storageId());
                    return calculate(
                            position,
                            outbound.getOrDefault(key, 0L),
                            lastWeekOutbound.getOrDefault(key, 0L),
                            disposal.getOrDefault(key, 0L),
                            weeks
                    );

                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Item::daysUntilStockout,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(MAX_RESULTS)
                .toList();
        return new ReorderSuggestionResponse(items, weeks);

    }

    private Map<Key, Long> toQuantityMap(List<ConsumptionSummaryRow> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> new Key(row.packageUnitId(), row.storageId()),
                ConsumptionSummaryRow::totalQuantity));
    }

    private Item calculate(StockPositionRow position, long outboundTotal,
                           long lastWeekOutbound, long disposalTotal, int weeks) {
        if (outboundTotal <= 0) {
            return null;
        }

        double weeklyAverage = (double) outboundTotal / weeks; // 최근 주간 평균 출고량
        double trend = Math.clamp(lastWeekOutbound / weeklyAverage, TREND_MIN, TREND_MAX); // 지난주 출고량 기반 추세 보정
        double expectNextWeek = weeklyAverage * trend;

        long available = Math.max(0, position.currentQuantity() - position.expiringSoonQuantity()); // 가용 재고 계산
        int threshold = position.threshold() == null ? 0 : position.threshold();

        double shortage = expectNextWeek + threshold - available;

        double disposalRate = (double) disposalTotal / (outboundTotal + disposalTotal);
        double adjustment = 1 - Math.min(disposalRate, MAX_DISPOSAL_ADJUSTMENT);

        int suggested = (int) Math.ceil(shortage * adjustment);

        if (suggested <= 0) {
            return null;
        }

        double dailyAverage = weeklyAverage / DAYS_PER_WEEK;

        return new Item(
                position.productName(),
                position.packUnit(),
                position.storageName(),
                suggested,
                position.currentQuantity().intValue(),
                position.expiringSoonQuantity().intValue(),
                weeklyAverage,
                (int) lastWeekOutbound,
                (int) disposalTotal,
                disposalRate,
                disposalTotal > 0,
                position.threshold(),
                (int) Math.floor(available / dailyAverage)
        );

    }
}