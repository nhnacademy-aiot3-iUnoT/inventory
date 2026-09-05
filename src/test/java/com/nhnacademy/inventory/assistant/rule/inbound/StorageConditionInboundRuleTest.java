package com.nhnacademy.inventory.assistant.rule.inbound;

import com.nhnacademy.inventory.assistant.domain.Severity;
import com.nhnacademy.inventory.assistant.domain.TargetType;
import com.nhnacademy.inventory.assistant.dto.EnvRange;
import com.nhnacademy.inventory.assistant.event.StockInboundCompletedEvent;
import com.nhnacademy.inventory.assistant.repository.AssistantEnvironmentRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StorageConditionInboundRuleTest {

    private static final Long ORGANIZATION_ID = 3L;
    private static final Long ZONE_ID = 1L;
    private static final Long PACK_UNIT_ID = 3L;
    private static final Long STORAGE_ID = 1L;

    @Mock
    private AssistantEnvironmentRepository assistantEnvironmentRepository;

    @Mock
    private MedicinePackageUnitRepository medicinePackageUnitRepository;

    @Mock
    private ZoneRepository zoneRepository;

    private StorageConditionInboundRule rule;

    @BeforeEach
    void setUp() {
        rule = new StorageConditionInboundRule(
                assistantEnvironmentRepository, medicinePackageUnitRepository, zoneRepository);

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
    @DisplayName("구역 상한이 의약품 상한보다 높으면 경고한다.")
    void evaluate_WhenZoneMaxExceedsMedicineMax_ReturnsFinding() {
        // 냉장 의약품(2~8)을 실온 구역(1~30)에 넣은 상황
        givenRanges(range("TEMPERATURE", "2", "8"), range("TEMPERATURE", "1", "30"));

        Optional<Finding> found = rule.evaluate(inboundEvent(), ORGANIZATION_ID);

        assertThat(found).isPresent();
        assertThat(found.get().type()).isEqualTo(FindingType.STORAGE_CONDITION);
        assertThat(found.get().severity()).isEqualTo(Severity.CRITICAL);
        assertThat(found.get().detail()).contains("온도 상한 30 > 기준 8");
        assertThat(found.get().target().type()).isEqualTo(TargetType.ZONE);
        assertThat(found.get().target().storageId()).isEqualTo(STORAGE_ID);
    }

    @Test
    @DisplayName("구역이 더 좁게 관리되면 경고하지 않는다.")
    void evaluate_WhenZoneIsStricter_ReturnsEmpty() {
        // 실온 의약품(1~30)을 냉장 구역(2~8)에 넣은 상황. 낭비일 뿐 사고는 아니다.
        givenRanges(range("TEMPERATURE", "1", "30"), range("TEMPERATURE", "2", "8"));

        assertThat(rule.evaluate(inboundEvent(), ORGANIZATION_ID)).isEmpty();
    }

    @Test
    @DisplayName("범위가 같으면 경고하지 않는다.")
    void evaluate_WhenRangesMatch_ReturnsEmpty() {
        givenRanges(range("TEMPERATURE", "2", "8"), range("TEMPERATURE", "2", "8"));

        assertThat(rule.evaluate(inboundEvent(), ORGANIZATION_ID)).isEmpty();
    }

    @Test
    @DisplayName("구역 하한이 의약품 하한보다 낮으면 경고한다.")
    void evaluate_WhenZoneMinFallsBelow_ReturnsFinding() {
        givenRanges(range("TEMPERATURE", "2", "8"), range("TEMPERATURE", "-5", "8"));

        Optional<Finding> found = rule.evaluate(inboundEvent(), ORGANIZATION_ID);

        assertThat(found).isPresent();
        assertThat(found.get().detail()).contains("온도 하한 -5 < 기준 2");
    }

    @Test
    @DisplayName("구역 기준이 설정되지 않은 항목은 판단하지 않는다.")
    void evaluate_WhenZoneThresholdIsNull_ReturnsEmpty() {
        // null 은 제한 없음이 아니라 미설정이다.
        givenRanges(range("TEMPERATURE", "2", "8"), new EnvRange("TEMPERATURE", null, null));

        assertThat(rule.evaluate(inboundEvent(), ORGANIZATION_ID)).isEmpty();
    }

    @Test
    @DisplayName("의약품 보관 기준이 없으면 판단하지 않는다.")
    void evaluate_WhenMedicineStandardAbsent_ReturnsEmpty() {
        given(assistantEnvironmentRepository.findMedicineRanges(ORGANIZATION_ID, PACK_UNIT_ID))
                .willReturn(List.of());

        assertThat(rule.evaluate(inboundEvent(), ORGANIZATION_ID)).isEmpty();
    }

    @Test
    @DisplayName("구역에 대응하는 항목이 없으면 그 항목은 건너뛴다.")
    void evaluate_WhenZoneHasNoMatchingType_ReturnsEmpty() {
        givenRanges(range("TEMPERATURE", "2", "8"), range("HUMIDITY", "30", "60"));

        assertThat(rule.evaluate(inboundEvent(), ORGANIZATION_ID)).isEmpty();
    }

    @Test
    @DisplayName("온도와 습도가 모두 벗어나면 둘 다 알린다.")
    void evaluate_WhenMultipleTypesBreach_ListsAll() {
        given(assistantEnvironmentRepository.findMedicineRanges(ORGANIZATION_ID, PACK_UNIT_ID))
                .willReturn(List.of(range("TEMPERATURE", "2", "8"), range("HUMIDITY", "30", "60")));
        given(assistantEnvironmentRepository.findZoneRanges(ZONE_ID))
                .willReturn(List.of(range("TEMPERATURE", "1", "30"), range("HUMIDITY", "20", "80")));

        Optional<Finding> found = rule.evaluate(inboundEvent(), ORGANIZATION_ID);

        assertThat(found).isPresent();
        assertThat(found.get().detail()).contains("온도 상한 30 > 기준 8");
        assertThat(found.get().detail()).contains("습도 상한 80 > 기준 60");
        assertThat(found.get().detail()).contains("습도 하한 20 < 기준 30");
    }

    private void givenRanges(EnvRange medicine, EnvRange zone) {
        given(assistantEnvironmentRepository.findMedicineRanges(ORGANIZATION_ID, PACK_UNIT_ID))
                .willReturn(List.of(medicine));
        given(assistantEnvironmentRepository.findZoneRanges(ZONE_ID))
                .willReturn(List.of(zone));
    }

    private EnvRange range(String type, String min, String max) {
        return new EnvRange(type, new BigDecimal(min), new BigDecimal(max));
    }

    private StockInboundCompletedEvent inboundEvent() {
        return new StockInboundCompletedEvent(
                UUID.randomUUID(), ZONE_ID, PACK_UNIT_ID, LocalDate.now().plusMonths(6), 10);
    }

}
