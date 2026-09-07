package com.nhnacademy.inventory.assistant.repository;

import com.nhnacademy.inventory.assistant.dto.EnvRange;
import com.nhnacademy.inventory.medicines.enviroment.domain.MedicineEnvironmentType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 보관 조건 비교용 전용 조회. 기존 리포지토리를 건드리지 않으려고 따로 둠
public interface AssistantEnvironmentRepository extends Repository<MedicineEnvironmentType, Long> {

    // 조직이 정한 품목 보관 기준
    @Query("""
            SELECT new com.nhnacademy.inventory.assistant.dto.EnvRange(
                       CAST(t.environmentType AS string), t.min, t.max)
            FROM MedicineEnvironmentType t
            WHERE t.medicineEnvironmentStandard.organization.id = :organizationId
              AND t.medicineEnvironmentStandard.medicinePackageUnit.id = :medicinePackageUnitId
            """)
    List<EnvRange> findMedicineRanges(
            @Param("organizationId") Long organizationId,
            @Param("medicinePackageUnitId") Long medicinePackageUnitId);

    // 구역에 설정된 환경 기준
    @Query("""
            SELECT new com.nhnacademy.inventory.assistant.dto.EnvRange(
                       zt.sensorType.name, zt.minValue, zt.maxValue)
            FROM ZoneThreshold zt
            WHERE zt.zone.id = :zoneId
            """)
    List<EnvRange> findZoneRanges(@Param("zoneId") Long zoneId);
}
