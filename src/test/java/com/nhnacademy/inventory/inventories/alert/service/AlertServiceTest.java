package com.nhnacademy.inventory.inventories.alert.service;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.global.util.UserContext;
import com.nhnacademy.inventory.inventories.alert.domain.Alert;
import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import com.nhnacademy.inventory.inventories.alert.dto.AlertDeleteRequest;
import com.nhnacademy.inventory.inventories.alert.dto.AlertInfoResponse;
import com.nhnacademy.inventory.inventories.alert.dto.AlertCheckRequest;
import com.nhnacademy.inventory.inventories.alert.dto.AlertSearchCondition;
import com.nhnacademy.inventory.inventories.alert.exception.AlertNotFoundException;
import com.nhnacademy.inventory.inventories.alert.repository.AlertRepository;
import com.nhnacademy.inventory.inventories.inventory.service.InventoryService;
import com.nhnacademy.inventory.inventories.threshold.domain.StockThreshold;
import com.nhnacademy.inventory.inventories.threshold.repository.StockThresholdRepository;
import com.nhnacademy.inventory.medicines.medicine.domain.Medicine;
import com.nhnacademy.inventory.medicines.medicine.domain.MedicinePackageUnit;
import com.nhnacademy.inventory.medicines.medicine.repository.MedicinePackageUnitRepository;
import com.nhnacademy.inventory.organizations.member.domain.OrganizationMember;
import com.nhnacademy.inventory.organizations.member.repository.OrganizationMemberRepository;
import com.nhnacademy.inventory.organizations.organization.domain.Organization;
import com.nhnacademy.inventory.organizations.storage.domain.Storage;
import com.nhnacademy.inventory.organizations.zone.domain.Zone;
import com.nhnacademy.inventory.organizations.zone.repository.ZoneRepository;
import com.nhnacademy.inventory.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private AlertRepository alertRepository;
    @Mock
    private OrganizationMemberRepository memberRepository;
    @Mock
    private ZoneRepository zoneRepository;
    @Mock
    private MedicinePackageUnitRepository medicinePackageUnitRepository;
    @Mock
    private StockThresholdRepository stockThresholdRepository;
    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private AlertService alertService;

    private Organization organization;
    private OrganizationMember approvedMember;
    private Storage storage;
    private Zone zone;
    private Medicine medicine;
    private MedicinePackageUnit packageUnit;

    @BeforeEach
    void setUp() throws Exception {
        organization = TestFixtures.createOrganization("테스트 조직1", "0123456789");
        setId(organization, 1L);

        approvedMember = TestFixtures.createOrganizationMember(organization);
        setId(approvedMember, 11L);

        storage = TestFixtures.createStorage(organization, "테스트 저장소1");
        setId(storage, 111L);

        zone = TestFixtures.createZone(storage, "테스트 구역1");
        setId(zone, 1111L);

        medicine = TestFixtures.createMedicine("1234", "테스트 의약품");
        setId(medicine, 2L);

        packageUnit = TestFixtures.createPackageUnit(medicine);
        setId(packageUnit, 22L);

    }

    @Nested
    @DisplayName("최소 재고 알림 생성 테스트")
    class createLowStockAlert{

        @Test
        @DisplayName("성공(총개수 < 임계값) 생성 테스트")
        void success_create() {
            StockThreshold stockThreshold = TestFixtures.createStockThreshold(storage, packageUnit, 50);

            given(zoneRepository.findById(zone.getId())).willReturn(Optional.of(zone));
            given(medicinePackageUnitRepository.findById(packageUnit.getId())).willReturn(Optional.of(packageUnit));
            given(stockThresholdRepository.findByStorageAndMedicinePackageUnit(storage, packageUnit))
                    .willReturn(Optional.of(stockThreshold));

            given(inventoryService.getTotalQuantity(storage.getId(), packageUnit.getId())).willReturn(30L);
            given(memberRepository.findMemberByStorageId(storage.getId())).willReturn(List.of(approvedMember));

            ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);

            alertService.createLowStockAlert(zone.getId(), packageUnit.getId());

            verify(alertRepository).save(alertCaptor.capture());
            Alert savedAlert = alertCaptor.getValue();

            assertAll(
                    () -> assertEquals(approvedMember, savedAlert.getOrganizationMember()),
                    () -> assertEquals(AlertType.LOW_STOCK, savedAlert.getAlertType()),
                    () -> assertEquals(false, savedAlert.getIsChecked())
            );
        }

        @Test
        @DisplayName("성공(총개수 >= 임계값) 스킵 테스트")
        void success_skip() {
            StockThreshold stockThreshold = TestFixtures.createStockThreshold(storage, packageUnit, 20);

            given(zoneRepository.findById(zone.getId())).willReturn(Optional.of(zone));
            given(medicinePackageUnitRepository.findById(packageUnit.getId())).willReturn(Optional.of(packageUnit));
            given(stockThresholdRepository.findByStorageAndMedicinePackageUnit(storage, packageUnit))
                    .willReturn(Optional.of(stockThreshold));
            given(inventoryService.getTotalQuantity(storage.getId(), packageUnit.getId())).willReturn(30L);

            assertDoesNotThrow(
                    () -> alertService.createLowStockAlert(zone.getId(), packageUnit.getId())
            );

            verify(alertRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 구역")
        void fail_NotFoundZone() {
            given(zoneRepository.findById(zone.getId())).willReturn(Optional.empty());

            assertThrowsExactly(IllegalArgumentException.class,
                    () -> alertService.createLowStockAlert(zone.getId(), packageUnit.getId()));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 단위의약품")
        void fail_NotFoundPackageUnit() {
            given(zoneRepository.findById(zone.getId())).willReturn(Optional.of(zone));
            given(medicinePackageUnitRepository.findById(packageUnit.getId())).willReturn(Optional.empty());

            assertThrowsExactly(IllegalArgumentException.class,
                    () -> alertService.createLowStockAlert(zone.getId(), packageUnit.getId()));
        }
    }

    @Nested
    @DisplayName("알림 조건 조회 테스트")
    class getAlerts{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            AlertSearchCondition condition = new AlertSearchCondition(AlertType.LOW_STOCK, false);
            Pageable pageable = PageRequest.of(0, 10);
            List<AlertInfoResponse> responses = List.of(new AlertInfoResponse(1L, organization.getId(),
                    "테스트 조직", AlertType.LOW_STOCK,"테스트 메시지", false, LocalDateTime.now()));

            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.of(approvedMember));
            given(alertRepository.searchByCondition(approvedMember, condition, pageable))
                    .willReturn(new PageImpl<>(responses, pageable, 1));

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            Page<AlertInfoResponse> actual = alertService.getAlerts(condition, pageable);

            assertAll(
                    () -> assertNotNull(actual),
                    () -> assertEquals(1, actual.getContent().size()),
                    () -> assertEquals(1, actual.getTotalElements())
            );

            verify(alertRepository).searchByCondition(approvedMember, condition, pageable);
        }

        @Test
        @DisplayName("성공 테스트 (빈리스트 반환)")
        void success_empty() {
            AlertSearchCondition condition = new AlertSearchCondition(AlertType.LOW_STOCK, false);
            Pageable pageable = PageRequest.of(0, 10);
            List<AlertInfoResponse> responses = List.of();

            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.of(approvedMember));
            given(alertRepository.searchByCondition(approvedMember, condition, pageable))
                    .willReturn(new PageImpl<>(responses, pageable, 0));

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            Page<AlertInfoResponse> actual = alertService.getAlerts(condition, pageable);

            assertAll(
                    () -> assertNotNull(actual),
                    () -> assertEquals(0, actual.getContent().size()),
                    () -> assertEquals(0, actual.getTotalElements())
            );

            verify(alertRepository).searchByCondition(approvedMember, condition, pageable);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() {
            AlertSearchCondition condition = new AlertSearchCondition(AlertType.LOW_STOCK, false);
            Pageable pageable = PageRequest.of(0, 10);

            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.empty());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ForbiddenException.class,
                    () -> alertService.getAlerts(condition, pageable));

            verify(alertRepository, never()).searchByCondition(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("읽지 않은 알림 개수 조회 테스트")
    class getUnreadAlertCount{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.of(approvedMember));
            given(alertRepository.countByOrganizationMemberAndIsChecked(approvedMember, false))
                    .willReturn(5L);

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            Long actual = alertService.getUncheckedAlertCount();

            assertNotNull(actual);
            assertEquals(5L, actual);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() {
            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.empty());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ForbiddenException.class,
                    () -> alertService.getUncheckedAlertCount());

            verify(alertRepository, never()).countByOrganizationMemberAndIsChecked(any(), anyBoolean());
        }
    }

    @Nested
    @DisplayName("다중 알림 읽음 표시 테스트")
    class markAsRead{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            List<Long> ids = List.of(1L, 2L);
            AlertCheckRequest request = new AlertCheckRequest(ids);
            Alert alert1 = TestFixtures.createAlert(approvedMember, AlertType.LOW_STOCK, "테스트 메시지1", false);
            Alert alert2 = TestFixtures.createAlert(approvedMember, AlertType.ENV_WARNING, "테스트 메시지2", false);
            List<Alert> alerts = List.of(alert1, alert2);

            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.of(approvedMember));
            given(alertRepository.findAllByIdInAndOrganizationMember(ids, approvedMember))
                    .willReturn(alerts);

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertDoesNotThrow(() -> alertService.markAsChecked(request));

            assertAll(
                    () -> assertEquals("테스트 메시지1", alert1.getMessage()),
                    () -> assertEquals(true, alert1.getIsChecked()),
                    () -> assertEquals("테스트 메시지2", alert2.getMessage()),
                    () -> assertEquals(true, alert2.getIsChecked())
            );
        }

        @Test
        @DisplayName("실패 - 알림 개수 불일치")
        void fail_NotFoundAlert() {
            List<Long> ids = List.of(1L, 2L);
            AlertCheckRequest request = new AlertCheckRequest(ids);
            Alert alert1 = TestFixtures.createAlert(approvedMember, AlertType.LOW_STOCK, "테스트 메시지1", false);
            List<Alert> alerts = List.of(alert1);

            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.of(approvedMember));
            given(alertRepository.findAllByIdInAndOrganizationMember(ids, approvedMember))
                    .willReturn(alerts);

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(AlertNotFoundException.class,
                    () -> alertService.markAsChecked(request));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() {
            List<Long> ids = List.of(1L, 2L);
            AlertCheckRequest request = new AlertCheckRequest(ids);

            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.empty());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ForbiddenException.class,
                    () -> alertService.markAsChecked(request));
        }
    }

    @Nested
    @DisplayName("다중 알림 삭제 테스트")
    class deleteAlerts{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            List<Long> ids = List.of(1L, 2L);
            AlertDeleteRequest request = new AlertDeleteRequest(ids);
            Alert alert1 = TestFixtures.createAlert(approvedMember, AlertType.LOW_STOCK, "테스트 메시지1", false);
            Alert alert2 = TestFixtures.createAlert(approvedMember, AlertType.ENV_WARNING, "테스트 메시지2", false);
            List<Alert> alerts = List.of(alert1, alert2);

            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.of(approvedMember));
            given(alertRepository.findAllByIdInAndOrganizationMember(ids, approvedMember))
                    .willReturn(alerts);

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertDoesNotThrow(() -> alertService.deleteAlerts(request));

            verify(alertRepository).deleteAll(alerts);
        }

        @Test
        @DisplayName("실패 - 알림 개수 불일치")
        void fail_NotFoundAlert() {
            List<Long> ids = List.of(1L, 2L);
            AlertDeleteRequest request = new AlertDeleteRequest(ids);
            Alert alert1 = TestFixtures.createAlert(approvedMember, AlertType.LOW_STOCK, "테스트 메시지1", false);
            List<Alert> alerts = List.of(alert1);

            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.of(approvedMember));
            given(alertRepository.findAllByIdInAndOrganizationMember(ids, approvedMember))
                    .willReturn(alerts);

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(AlertNotFoundException.class,
                    () -> alertService.deleteAlerts(request));

            verify(alertRepository, never()).deleteAll(alerts);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() {
            List<Long> ids = List.of(1L, 2L);
            AlertDeleteRequest request = new AlertDeleteRequest(ids);

            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.empty());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ForbiddenException.class,
                    () -> alertService.deleteAlerts(request));

            verify(alertRepository, never()).deleteAll(any());
        }
    }

    @Nested
    @DisplayName("전체 알림 삭제 테스트")
    class deleteAllAlerts{

        @Test
        @DisplayName("성공 테스트")
        void success() {
            Alert alert1 = TestFixtures.createAlert(approvedMember, AlertType.LOW_STOCK, "테스트 메시지1", false);
            Alert alert2 = TestFixtures.createAlert(approvedMember, AlertType.ENV_WARNING, "테스트 메시지2", false);
            List<Alert> alerts = List.of(alert1, alert2);

            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.of(approvedMember));
            given(alertRepository.findAllByOrganizationMember(approvedMember))
                    .willReturn(alerts);

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertDoesNotThrow(() -> alertService.deleteAllAlerts());

            verify(alertRepository).deleteAll(alerts);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() {
            given(memberRepository.findByAccountUuid(approvedMember.getAccountUuid()))
                    .willReturn(Optional.empty());

            UserContext.setUserUuid(approvedMember.getAccountUuid());

            assertThrowsExactly(ForbiddenException.class,
                    () -> alertService.deleteAllAlerts());

            verify(alertRepository, never()).deleteAll(any());
        }
    }

    private void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}