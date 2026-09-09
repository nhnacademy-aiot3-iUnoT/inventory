package com.nhnacademy.inventory.inventories.inventory.operation.inbound.service;


import com.nhnacademy.inventory.inventories.inventory.domain.MedicineInventory;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.domain.InboundOperation;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.MedicineInboundRequest;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.zone.domain.EnvStatus;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.domain.ZoneStatus;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;


import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class InboundServiceTest {

    @Mock
    MedicinePackageUnitRepository medicinePackageUnitRepository;
    @Mock
    ZoneRepository zoneRepository;
    @Mock
    MedicineInventoryRepository medicineInventoryRepository;
    @Mock
    InboundOperation inboundOperation;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    InboundService inboundService;



    @Test
    @DisplayName("입고 등록 ")
    void createInbound() {


        MedicineInboundRequest request = new MedicineInboundRequest(
                1L,
                1L,
                "ABC-123",
                LocalDate.now(),
                30,
                null,
                TransactionType.INBOUND

        );

        Medicine medicine = Medicine.create(
                "123456789",
                "product-test",
                "storage-test",
                "validity-test",
                null,
                "company-test"
        );

        MedicinePackageUnit medicinePackageUnit =
                MedicinePackageUnit.create(
                        medicine,
                        "10ml"

                );

        Zone zone = Zone.builder()
                .name("zone-test")
                .description("description-test")
                .status(ZoneStatus.ACTIVE)
                .envStatus(EnvStatus.NORMAL)
                .build();

        MedicineInventory medicineInventory = MedicineInventory.create(
                medicinePackageUnit,
                zone,
                "ABC-123",
                LocalDate.now(),
                20
        );



        given(medicinePackageUnitRepository.findById(request.medicinePackageUnitId()))
                .willReturn(Optional.of(medicinePackageUnit));

        ReflectionTestUtils.setField(medicinePackageUnit,"id",1L);
        ReflectionTestUtils.setField(zone,"id",1L);


        given(zoneRepository.findById(request.zoneId())).willReturn(Optional.of(zone));
        given(medicineInventoryRepository.findByMedicinePackageUnitIdAndZoneIdAndLotNumber
                (request.medicinePackageUnitId(),
                        request.zoneId(),
                        request.lotNumber()
                )
        ).willReturn(Optional.of(medicineInventory));


        inboundService.createInbound(request);

        verify(inboundOperation).inboundInventory(
                medicinePackageUnit,
                zone,
                medicineInventory,
                request
                );





    }
}