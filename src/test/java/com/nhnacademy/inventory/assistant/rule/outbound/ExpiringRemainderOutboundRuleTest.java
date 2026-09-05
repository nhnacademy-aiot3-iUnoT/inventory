package com.nhnacademy.inventory.assistant.rule.outbound;

import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.dto.StockLot;
import com.nhnacademy.inventory.assistant.event.StockOutboundCompletedEvent;
import com.nhnacademy.inventory.assistant.repository.AssistantStockRepository;
import com.nhnacademy.inventory.assistant.rule.Finding;
import com.nhnacademy.inventory.assistant.rule.FindingType;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * 심각도 경계가 -1 / 0 / 7 / 30 네 군데라 하나만 어긋나도 조용히 틀린 알림이 나간다.
 * 경계마다 값을 하나씩 확인한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExpiringRemainderOutboundRuleTest {

    private static final Long ZONE_ID = 1L;
    private static final Long PACK_UNIT_ID = 3L;
    private static final Long STORAGE_ID = 1L;

    @Mock
    private AssistantStockRepository assistantStockRepository;

    @Mock
    private MedicinePackageUnitRepository medicinePackageUnitRepository;

    @Mock
    private ZoneRepository zoneRepository;

    private ExpiringRemainderOutboundRule rule;

    @BeforeEach
    void setUp() {
        rule = new ExpiringRemainderOutboundRule(
                assistantStockRepository, medicinePackageUnitRepository, zoneRepository);

        Storage storage = mock(Storage.class);
        given(storage.getId()).willReturn(STORAGE_ID);

        Zone zone = mock(Zone.class);
        given(zone.getId()).willReturn(ZONE_ID);
        given(zone.getName()).willReturn("구역1");
        given(zone.getStorage()).willReturn(storage);

        given(zoneRepository.findById(ZONE_ID)).willReturn(Optional.of(zone));
        given(medicinePackageUnitRepository.findById(anyLong())).willReturn(Optional.empty());
    }

    @Test
    @DisplayName("남은 재고가 없으면 알리지 않는다.")
    void evaluate_WhenNothingRemains_ReturnsEmpty() {
        givenRemaining();

        assertThat(rule.evaluate(event(), 1L)).isEmpty();
    }

    @Test
    @DisplayName("유통기한이 31일 남으면 알리지 않는다.")
    void evaluate_WhenBeyondInfoWindow_ReturnsEmpty() {
        givenRemainingInDays(31);

        assertThat(rule.evaluate(event(), 1L)).isEmpty();
    }

    @Test
    @DisplayName("유통기한이 30일 남으면 INFO로 알린다.")
    void evaluate_WhenExactlyInfoWindow_ReturnsInfo() {
        assertThat(severityOf(30)).isEqualTo(Severity.INFO);
    }

    @Test
    @DisplayName("유통기한이 8일 남으면 INFO로 알린다.")
    void evaluate_WhenJustOutsideWarnWindow_ReturnsInfo() {
        assertThat(severityOf(8)).isEqualTo(Severity.INFO);
    }

    @Test
    @DisplayName("유통기한이 7일 남으면 WARN으로 알린다.")
    void evaluate_WhenExactlyWarnWindow_ReturnsWarn() {
        assertThat(severityOf(7)).isEqualTo(Severity.WARN);
    }

    @Test
    @DisplayName("유통기한이 오늘까지면 WARN으로 알린다.")
    void evaluate_WhenExpiresToday_ReturnsWarn() {
        assertThat(severityOf(0)).isEqualTo(Severity.WARN);
    }

    @Test
    @DisplayName("이미 만료되었으면 CRITICAL로 알린다.")
    void evaluate_WhenAlreadyExpired_ReturnsCritical() {
        assertThat(severityOf(-1)).isEqualTo(Severity.CRITICAL);
    }

    @Test
    @DisplayName("만료된 재고는 폐기하라는 안내를 담는다.")
    void evaluate_WhenExpired_ExplainsDisposal() {
        givenRemainingInDays(-3);

        Optional<Finding> found = rule.evaluate(event(), 1L);

        assertThat(found).isPresent();
        assertThat(found.get().type()).isEqualTo(FindingType.EXPIRING_STOCK);
        assertThat(found.get().explanation()).contains("만료");
        assertThat(found.get().target().storageId()).isEqualTo(STORAGE_ID);
    }

    @Test
    @DisplayName("유통기한이 가장 빠른 로트를 기준으로 판정한다.")
    void evaluate_UsesEarliestLot() {
        // 리포지토리가 유통기한 오름차순으로 돌려주므로 첫 번째가 기준이 된다.
        given(assistantStockRepository.findRemainingLots(ZONE_ID, PACK_UNIT_ID))
                .willReturn(List.of(lot(3), lot(60)));

        Optional<Finding> found = rule.evaluate(event(), 1L);

        assertThat(found).isPresent();
        assertThat(found.get().severity()).isEqualTo(Severity.WARN);
    }

    private Severity severityOf(int daysLeft) {
        givenRemainingInDays(daysLeft);

        Optional<Finding> found = rule.evaluate(event(), 1L);

        assertThat(found).isPresent();

        return found.get().severity();
    }

    private void givenRemaining(StockLot... lots) {
        given(assistantStockRepository.findRemainingLots(ZONE_ID, PACK_UNIT_ID)).willReturn(List.of(lots));
    }

    private void givenRemainingInDays(int daysLeft) {
        givenRemaining(lot(daysLeft));
    }

    private StockLot lot(int daysLeft) {
        return new StockLot(LocalDate.now().plusDays(daysLeft), "LOT-" + daysLeft, 10);
    }

    private StockOutboundCompletedEvent event() {
        return new StockOutboundCompletedEvent(UUID.randomUUID(), ZONE_ID, PACK_UNIT_ID, 5);
    }
}
