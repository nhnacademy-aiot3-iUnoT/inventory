package com.nhnacademy.inventory.inventories.inventory.service;

import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    ZoneRepository zoneRepository;
    @Mock
    MedicineInventoryRepository medicineInventoryRepository;

    @InjectMocks
    InventoryService inventoryService;

    @Test
    @DisplayName("storage-packUnitId 합계 조회")
    void getTotalQuantity() {

        Zone zone = mock(Zone.class);
        List<Zone> zones = List.of(zone);

        given(zoneRepository.findAllByStorageId(1L)).willReturn(zones);
        given(medicineInventoryRepository.findByZoneAndPackUnitSum(zones,1L))
                .willReturn(50L);

        Long total = inventoryService.getTotalQuantity(1L,1L);

        verify(zoneRepository).findAllByStorageId(anyLong());
        verify(medicineInventoryRepository).findByZoneAndPackUnitSum(anyList(),anyLong());


        assertEquals(50L,total);


    }

    @Test
    @DisplayName("구역이 없는 경우")
    void zoneNotExists(){

        given(zoneRepository.findAllByStorageId(1L)).willReturn(List.of());

        Long total = inventoryService.getTotalQuantity(1L,1L);

        verify(zoneRepository).findAllByStorageId(1L);

        assertEquals(0L,total);



    }



}