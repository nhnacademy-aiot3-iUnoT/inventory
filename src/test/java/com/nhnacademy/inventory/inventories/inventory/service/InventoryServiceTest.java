package com.nhnacademy.inventory.inventories.inventory.service;

import com.nhnacademy.inventory.inventories.inventory.dto.InventoryInfoResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoryResponse;
import com.nhnacademy.inventory.inventories.inventory.exception.InventoryNotFoundException;
import com.nhnacademy.inventory.inventories.inventory.repository.MedicineInventoryRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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


    @Test
    @DisplayName("상세 재고 조회")
    void getInventoryInfo() {


        Zone zone = mock(Zone.class);

        List<Zone> zones = List.of(zone);
        Pageable pageable = Pageable.ofSize(10);
        Page<InventoryResponse> page = new PageImpl<>(
                List.of(mock(InventoryResponse.class)),pageable,1);


        given(zone.getId()).willReturn(1L);



        given(zoneRepository.findAllByStorageId(1L)).willReturn(zones);
        given(medicineInventoryRepository.findByZonesAndPackUnitId(List.of(1L),1L,pageable))
                .willReturn(page);

        InventoryInfoResponse info =  inventoryService.getInventoryInfo(1L,1L,pageable);

        assertNotNull(info);

        verify(zoneRepository).findAllByStorageId(1L);
        verify(medicineInventoryRepository).findByZonesAndPackUnitId(List.of(1L),1L,pageable);






    }


    @Test
    @DisplayName("의약품 재고가 없을 경우")
    void inventoryIsNotExist(){

        Pageable pageable = Pageable.ofSize(10);

        Zone zone = mock(Zone.class);

        given(zone.getId()).willReturn(1L);


        given(zoneRepository.findAllByStorageId(1L)).willReturn(List.of(zone));
        given(medicineInventoryRepository.findByZonesAndPackUnitId(List.of(1L),1L,pageable))
                .willReturn(Page.empty(pageable));

        assertThrows(InventoryNotFoundException.class, () -> inventoryService.getInventoryInfo(1L,1L,pageable));


    }



}