package com.nhnacademy.inventory.inventories.threshold.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.inventories.threshold.domain.StockThreshold;
import com.nhnacademy.inventory.inventories.threshold.dto.StockThresholdInfoResponse;
import com.nhnacademy.inventory.inventories.threshold.dto.StockThresholdSaveRequest;
import com.nhnacademy.inventory.inventories.threshold.dto.StockThresholdUpdateRequest;
import com.nhnacademy.inventory.inventories.threshold.exception.StockThresholdNotFoundException;
import com.nhnacademy.inventory.inventories.threshold.repository.StockThresholdRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.storage.service.StorageService;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockThresholdServiceTest {

    @Mock
    private StockThresholdRepository stockThresholdRepository;
    @Mock
    private MedicinePackageUnitRepository medicinePackageUnitRepository;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private StockThresholdService stockThresholdService;

    private Organization organization;
    private Medicine medicine;
    private Storage storage;
    private MedicinePackageUnit medicinePackageUnit;
    private StockThreshold stockThreshold;

    @BeforeEach
    void setUp() throws Exception {
        organization = TestFixtures.createOrganization("테스트 조직1", "0123456789");
        setId(organization, 1L);

        medicine = TestFixtures.createMedicine("테스트 의약품 코드", "테스트 의약품1");
        setId(medicine, 11L);

        storage = TestFixtures.createStorage(organization, "테스트 저장소1");
        setId(storage, 111L);

        medicinePackageUnit = TestFixtures.createPackageUnit(medicine, "테스트 단위1");
        setId(medicinePackageUnit, 1111L);

        stockThreshold = TestFixtures.createStockThreshold(storage, medicinePackageUnit, 10);
        setId(stockThreshold, 11111L);
    }

    @Nested
    @DisplayName("최소 재고 임계값 생성 테스트")
    class saveStockThreshold{

        @Test
        @DisplayName("성공 테스트(없을 시 생성)")
        void success_create() {
            StockThresholdSaveRequest request = new StockThresholdSaveRequest(
                    medicinePackageUnit.getId(),
                    20
            );

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(medicinePackageUnitRepository.findById(medicinePackageUnit.getId()))
                    .willReturn(Optional.of(medicinePackageUnit));
            given(stockThresholdRepository.findByStorageAndMedicinePackageUnit(storage, medicinePackageUnit))
                    .willReturn(Optional.empty());
            given(stockThresholdRepository.save(any(StockThreshold.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            StockThresholdInfoResponse response = stockThresholdService.saveStockThreshold(
                    storage.getId(), request
            );

            assertAll(
                    () -> assertNotEquals(11111L, response.stockThresholdId()),
                    () -> assertEquals(111L, response.storageId()),
                    () -> assertEquals(1111L, response.medicinePackageUnitId()),
                    () -> assertEquals(20, response.stockThreshold()),
                    () -> assertEquals(true, response.isActive())
            );

            verify(stockThresholdRepository).save(any());
        }

        @Test
        @DisplayName("성공 테스트(있을 시 업데이트)")
        void success_update() {
            StockThresholdSaveRequest request = new StockThresholdSaveRequest(
                    medicinePackageUnit.getId(),
                    20
            );

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(medicinePackageUnitRepository.findById(medicinePackageUnit.getId()))
                    .willReturn(Optional.of(medicinePackageUnit));
            given(stockThresholdRepository.findByStorageAndMedicinePackageUnit(storage, medicinePackageUnit))
                    .willReturn(Optional.of(stockThreshold));

            StockThresholdInfoResponse response = stockThresholdService.saveStockThreshold(
                    storage.getId(), request
            );

            assertAll(
                    () -> assertEquals(11111L, response.stockThresholdId()),
                    () -> assertEquals(111L, response.storageId()),
                    () -> assertEquals(1111L, response.medicinePackageUnitId()),
                    () -> assertEquals(20, response.stockThreshold()),
                    () -> assertEquals(true, response.isActive())
            );

            verify(stockThresholdRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            StockThresholdSaveRequest request = new StockThresholdSaveRequest(
                    medicinePackageUnit.getId(),
                    20
            );

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willThrow(new ForbiddenException());

            assertThrowsExactly(ForbiddenException.class, () ->
                    stockThresholdService.saveStockThreshold(storage.getId(), request)
            );
        }
    }

    @Nested
    @DisplayName("최소 재고 임계값 조회 테스트")
    class getStockThresholds{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            given(storageService.validateMemberAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(stockThresholdRepository.findAllByStorage(storage))
                    .willReturn(List.of(stockThreshold));

            List<StockThresholdInfoResponse> responses = stockThresholdService.getStockThresholds(
                    storage.getId()
            );

            assertAll(
                    () -> assertEquals(1, responses.size()),
                    () -> assertEquals(11111L, responses.getFirst().stockThresholdId()),
                    () -> assertEquals(111L, responses.getFirst().storageId()),
                    () -> assertEquals(1111L, responses.getFirst().medicinePackageUnitId()),
                    () -> assertEquals(10, responses.getFirst().stockThreshold()),
                    () -> assertEquals(true, responses.getFirst().isActive())
            );
        }

        @Test
        @DisplayName("성공 테스트(빈 리스트)")
        void success_empty() {
            given(storageService.validateMemberAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(stockThresholdRepository.findAllByStorage(storage))
                    .willReturn(List.of());

            List<StockThresholdInfoResponse> responses = stockThresholdService.getStockThresholds(
                    storage.getId()
            );

            assertEquals(0, responses.size());
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            given(storageService.validateMemberAndGetStorage(storage.getId()))
                    .willThrow(new ForbiddenException());

            assertThrowsExactly(ForbiddenException.class, () ->
                    stockThresholdService.getStockThresholds(storage.getId())
            );
        }
    }

    @Nested
    @DisplayName("최소 재고 임계값 수정 테스트")
    class updateStockThreshold{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            StockThresholdUpdateRequest request = new StockThresholdUpdateRequest(30, true);

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(stockThresholdRepository.findByIdAndStorage(stockThreshold.getId(), storage))
                    .willReturn(Optional.of(stockThreshold));

            StockThresholdInfoResponse response = stockThresholdService.updateStockThreshold(
                    storage.getId(), stockThreshold.getId(), request
            );

            assertAll(
                    () -> assertEquals(11111L, response.stockThresholdId()),
                    () -> assertEquals(111L, response.storageId()),
                    () -> assertEquals(1111L, response.medicinePackageUnitId()),
                    () -> assertEquals(30, response.stockThreshold()),
                    () -> assertEquals(true, response.isActive())
            );
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            StockThresholdUpdateRequest request = new StockThresholdUpdateRequest(30, true);

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willThrow(new ForbiddenException());

            assertThrowsExactly(ForbiddenException.class, () ->
                    stockThresholdService.updateStockThreshold(storage.getId(), stockThreshold.getId(), request)
            );
        }

        @Test
        @DisplayName("실패 - 존재하지않는 최소 재고 임계값")
        void fail_NotFoundStockThreshold() {
            StockThresholdUpdateRequest request = new StockThresholdUpdateRequest(30, true);

            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(stockThresholdRepository.findByIdAndStorage(stockThreshold.getId(), storage))
                    .willReturn(Optional.empty());

            assertThrowsExactly(StockThresholdNotFoundException.class, () ->
                    stockThresholdService.updateStockThreshold(storage.getId(), stockThreshold.getId(), request)
            );
        }
    }

    @Nested
    @DisplayName("최소 재고 임계값 삭제 테스트")
    class deleteStockThreshold{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(stockThresholdRepository.findByIdAndStorage(stockThreshold.getId(), storage))
                    .willReturn(Optional.of(stockThreshold));

            stockThresholdService.deleteStockThreshold(storage.getId(), stockThreshold.getId());

            verify(stockThresholdRepository).delete(stockThreshold);
        }

        @Test
        @DisplayName("실패 - 권한없음")
        void fail_Forbidden() {
            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willThrow(new ForbiddenException());

            assertThrowsExactly(ForbiddenException.class, () ->
                    stockThresholdService.deleteStockThreshold(storage.getId(), stockThreshold.getId())
            );
        }

        @Test
        @DisplayName("실패 - 존재하지않는 최소 재고 임계값")
        void fail_NotFoundStockThreshold() {
            given(storageService.validateOwnerAndGetStorage(storage.getId()))
                    .willReturn(storage);
            given(stockThresholdRepository.findByIdAndStorage(stockThreshold.getId(), storage))
                    .willReturn(Optional.empty());

            assertThrowsExactly(StockThresholdNotFoundException.class, () ->
                    stockThresholdService.deleteStockThreshold(storage.getId(), stockThreshold.getId())
            );
        }
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}