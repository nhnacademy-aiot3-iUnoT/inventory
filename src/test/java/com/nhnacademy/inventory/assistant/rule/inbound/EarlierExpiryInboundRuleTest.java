package com.nhnacademy.inventory.assistant.rule.inbound;

import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.dto.StockLot;
import com.nhnacademy.inventory.assistant.event.StockInboundCompletedEvent;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EarlierExpiryInboundRuleTest {

    private static final Long ZONE_ID = 1L;
    private static final Long PACK_UNIT_ID = 3L;
    private static final Long STORAGE_ID = 1L;

    @Mock
    private AssistantStockRepository assistantStockRepository;

    @Mock
    private MedicinePackageUnitRepository medicinePackageUnitRepository;

    @Mock
    private ZoneRepository zoneRepository;

    private EarlierExpiryInboundRule rule;

    @BeforeEach
    void setUp() {
        rule = new EarlierExpiryInboundRule(
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
    @DisplayName("더 빠른 유통기한 재고가 없으면 알리지 않는다.")
    void evaluate_WhenNoEarlierLot_ReturnsEmpty() {
        givenEarlierLots();

        assertThat(rule.evaluate(event(), 1L)).isEmpty();
    }

    @Test
    @DisplayName("더 빠른 유통기한 재고가 있으면 먼저 쓰라고 알린다.")
    void evaluate_WhenEarlierLotExists_ReturnsFinding() {
        givenEarlierLots(lot(30, "LOT-A", 10));

        Optional<Finding> found = rule.evaluate(event(), 1L);

        assertThat(found).isPresent();
        assertThat(found.get().type()).isEqualTo(FindingType.EXPIRY_ORDER);
        assertThat(found.get().severity()).isEqualTo(Severity.INFO);
        assertThat(found.get().target().storageId()).isEqualTo(STORAGE_ID);
    }

    @Test
    @DisplayName("남은 재고가 이미 만료되었으면 CRITICAL로 알린다.")
    void evaluate_WhenEarliestLotAlreadyExpired_ReturnsCritical() {
        givenEarlierLots(lot(-1, "LOT-EXPIRED", 5));

        Optional<Finding> found = rule.evaluate(event(), 1L);

        assertThat(found).isPresent();
        assertThat(found.get().severity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    @DisplayName("오늘 만료되는 재고는 아직 만료로 보지 않는다.")
    void evaluate_WhenEarliestLotExpiresToday_IsNotCritical() {
        givenEarlierLots(lot(0, "LOT-TODAY", 5));

        Optional<Finding> found = rule.evaluate(event(), 1L);

        assertThat(found).isPresent();
        assertThat(found.get().severity()).isEqualTo(Severity.INFO);
    }

    @Test
    @DisplayName("여러 로트의 수량을 합쳐서 알린다.")
    void evaluate_SumsQuantityAcrossLots() {
        givenEarlierLots(lot(10, "LOT-A", 7), lot(20, "LOT-B", 13));

        Optional<Finding> found = rule.evaluate(event(), 1L);

        assertThat(found).isPresent();
        assertThat(found.get().detail()).contains("20");
    }

    @Test
    @DisplayName("구역을 찾지 못하면 알리지 않는다.")
    void evaluate_WhenZoneMissing_ReturnsEmpty() {
        givenEarlierLots(lot(10, "LOT-A", 10));
        given(zoneRepository.findById(any())).willReturn(Optional.empty());

        assertThat(rule.evaluate(event(), 1L)).isEmpty();
    }

    private void givenEarlierLots(StockLot... lots) {
        given(assistantStockRepository.findEarlierExpiryLots(anyLong(), anyLong(), any(LocalDate.class)))
                .willReturn(List.of(lots));
    }

    private StockLot lot(int daysFromNow, String lotNumber, int quantity) {
        return new StockLot(LocalDate.now().plusDays(daysFromNow), lotNumber, quantity);
    }

    private StockInboundCompletedEvent event() {
        return new StockInboundCompletedEvent(
                UUID.randomUUID(), ZONE_ID, PACK_UNIT_ID, LocalDate.now().plusMonths(12), 10);
    }
}
