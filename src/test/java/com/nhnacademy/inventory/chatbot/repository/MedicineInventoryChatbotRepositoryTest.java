package com.nhnacademy.inventory.chatbot.repository;

import com.nhnacademy.inventory.chatbot.dto.MedicinePackageUnitTargetRow;
import com.nhnacademy.inventory.chatbot.dto.ZoneTargetRow;
import com.nhnacademy.inventory.chatbot.dto.query.FindMedicinePackageUnitTargetQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindZoneTargetQuery;
import com.nhnacademy.inventory.chatbot.repository.impl.MedicineInventoryChatbotRepositoryImpl;
import com.nhnacademy.inventory.global.config.QuerydslConfig;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicineRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.organization.repository.OrganizationRepository;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.repository.StorageRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, MedicineInventoryChatbotRepositoryImpl.class})
@DisplayName("의약품 재고 챗봇 Repository 테스트")
class MedicineInventoryChatbotRepositoryTest {
    @Autowired
    private MedicineInventoryChatbotRepository inventoryRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private MedicinePackageUnitRepository packageUnitRepository;

    @Test
    @DisplayName("기존 재고가 없어도 의약품 포장단위와 접근 가능한 활성 구역을 조회한다")
    void findOperationTargetsWithoutExistingInventory() {
        Organization organization = organizationRepository.save(
                TestFixtures.createOrganization("테스트조직", "1234567890")
        );
        Storage accessibleStorage = storageRepository.save(
                TestFixtures.createStorage(organization, "본관창고")
        );
        Storage inaccessibleStorage = storageRepository.save(
                TestFixtures.createStorage(organization, "별관창고")
        );
        Zone accessibleZone = zoneRepository.save(
                TestFixtures.createZone(accessibleStorage, "냉장구역")
        );
        Zone inaccessibleZone = zoneRepository.save(
                TestFixtures.createZone(inaccessibleStorage, "냉장구역")
        );

        Medicine medicine = medicineRepository.save(
                TestFixtures.createMedicine("123456789", "타이레놀정")
        );
        MedicinePackageUnit packageUnit = packageUnitRepository.save(
                TestFixtures.createPackageUnit(medicine, "500mg 10정")
        );

        List<MedicinePackageUnitTargetRow> packageUnits = inventoryRepository.findPackageUnitTargets(
                new FindMedicinePackageUnitTargetQuery("타이레놀", "500mg", 5)
        );
        List<ZoneTargetRow> zones = inventoryRepository.findZoneTargets(
                new FindZoneTargetQuery(
                        List.of(accessibleStorage.getId()),
                        "본관",
                        "냉장",
                        5
                )
        );

        assertThat(packageUnits).containsExactly(new MedicinePackageUnitTargetRow(
                packageUnit.getId(),
                "타이레놀정",
                "500mg 10정"
        ));
        assertThat(zones).containsExactly(new ZoneTargetRow(
                accessibleZone.getId(),
                "본관창고",
                "냉장구역"
        ));
        assertThat(inventoryRepository.findPackageUnitTargetById(packageUnit.getId()))
                .contains(new MedicinePackageUnitTargetRow(
                        packageUnit.getId(),
                        "타이레놀정",
                        "500mg 10정"
                ));
        assertThat(inventoryRepository.findZoneTargetById(
                accessibleZone.getId(),
                List.of(accessibleStorage.getId())
        )).contains(new ZoneTargetRow(
                accessibleZone.getId(),
                "본관창고",
                "냉장구역"
        ));
        assertThat(inventoryRepository.findZoneTargetById(
                inaccessibleZone.getId(),
                List.of(accessibleStorage.getId())
        )).isEmpty();
    }

    @Test
    @DisplayName("포장단위의 공백과 구분기호가 달라도 핵심 단어로 후보를 조회한다")
    void findPackageUnitTargetWithDifferentFormatting() {
        Medicine medicine = medicineRepository.save(
                TestFixtures.createMedicine(
                        "2026090401",
                        "타이레놀정500밀리그람(아세트아미노펜)"
                )
        );
        MedicinePackageUnit packageUnit = packageUnitRepository.save(
                TestFixtures.createPackageUnit(
                        medicine,
                        "블리스터  - 10정/상자[10정/PTP*1]"
                )
        );

        List<MedicinePackageUnitTargetRow> result = inventoryRepository.findPackageUnitTargets(
                new FindMedicinePackageUnitTargetQuery(
                        "타이레놀정500밀리그람(아세트아미노펜)",
                        "블리스터 - 10정/상자",
                        5
                )
        );

        assertThat(result).containsExactly(new MedicinePackageUnitTargetRow(
                packageUnit.getId(),
                "타이레놀정500밀리그람(아세트아미노펜)",
                "블리스터  - 10정/상자[10정/PTP*1]"
        ));
    }
}
