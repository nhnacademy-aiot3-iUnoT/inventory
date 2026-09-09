package com.nhnacademy.inventory.chatbot.service;

import com.nhnacademy.inventory.chatbot.dto.MedicinePackageUnitTargetRow;
import com.nhnacademy.inventory.chatbot.dto.ZoneTargetRow;
import com.nhnacademy.inventory.chatbot.dto.query.FindMedicinePackageUnitTargetQuery;
import com.nhnacademy.inventory.chatbot.dto.query.FindZoneTargetQuery;
import com.nhnacademy.inventory.chatbot.dto.request.InboundToolRequest;
import com.nhnacademy.inventory.chatbot.dto.request.OutboundToolRequest;
import com.nhnacademy.inventory.chatbot.dto.response.InboundToolResponse;
import com.nhnacademy.inventory.chatbot.dto.response.OutboundToolResponse;
import com.nhnacademy.inventory.chatbot.repository.MedicineInventoryChatbotRepository;
import com.nhnacademy.inventory.inventories.inventory.exception.InsufficientStockException;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.MedicineInboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.service.InboundService;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.domain.OutboundReason;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundTargetResponse;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.dto.MedicineOutboundTargetResponse;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import com.nhnacademy.inventory.inventories.inventory.operation.outbound.service.MedicineOutboundService;
import com.nhnacademy.inventory.inventories.transaction.domain.TransactionType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("챗봇 입출고 작업 서비스 테스트")
class ChatbotInventoryOperationServiceTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @Mock
    private MedicineInventoryChatbotRepository inventoryRepository;

    @Mock
    private ChatbotStorageAccessService accessService;

    @Mock
    private InboundService inboundService;

    @Mock
    private MedicineOutboundService outboundService;

    private ChatbotInventoryOperationService operationService;

    @BeforeEach
    void setUp() {
        operationService = new ChatbotInventoryOperationService(
                inventoryRepository,
                accessService,
                inboundService,
                outboundService,
                VALIDATOR
        );
    }

    @Test
    @DisplayName("유일한 의약품과 구역이 검색되면 입고를 처리한다")
    void inbound() {
        InboundToolRequest request = inboundRequest();
        givenTargets();

        InboundToolResponse response = operationService.inbound(request);

        assertTrue(response.success());
        assertEquals("타이레놀정", response.medicineName());
        assertEquals(10, response.quantity());
        verify(inboundService).createInbound(new MedicineInboundRequest(
                11L,
                21L,
                "LOT-001",
                request.expirationDate(),
                10,
                "신규입고",
                request.transactionType()


        ));
        verify(inventoryRepository).findPackageUnitTargets(
                new FindMedicinePackageUnitTargetQuery("타이레놀", "500mg", 5)
        );
        verify(inventoryRepository).findZoneTargets(
                new FindZoneTargetQuery(List.of(1L), "A창고", "1구역", 5)
        );
    }

    @Test
    @DisplayName("선택한 ID가 있으면 변형된 이름 대신 ID로 입고 대상을 확정한다")
    void inboundWithSelectedIds() {
        InboundToolRequest request = new InboundToolRequest(
                11L,
                "변형된 의약품명",
                "블리스터 - 10정/상자",
                21L,
                "변형된 저장소명",
                "변형된 구역명",
                "LOT-001",
                LocalDate.now().plusDays(30),
                10,
                TransactionType.INBOUND,
                "신규 입고"
        );
        given(inventoryRepository.findPackageUnitTargetById(11L))
                .willReturn(Optional.of(new MedicinePackageUnitTargetRow(
                        11L,
                        "타이레놀정",
                        "블리스터  - 10정/상자"
                )));
        given(accessService.getAccessibleStorageIds()).willReturn(List.of(1L));
        given(inventoryRepository.findZoneTargetById(21L, List.of(1L)))
                .willReturn(Optional.of(new ZoneTargetRow(21L, "A창고", "1구역")));

        InboundToolResponse response = operationService.inbound(request);

        assertTrue(response.success());
        assertEquals("타이레놀정", response.medicineName());
        assertEquals("블리스터  - 10정/상자", response.packUnit());
        assertEquals("A창고", response.storageName());
        assertEquals("1구역", response.zoneName());
        verify(inventoryRepository, never())
                .findPackageUnitTargets(any(FindMedicinePackageUnitTargetQuery.class));
        verify(inventoryRepository, never())
                .findZoneTargets(any(FindZoneTargetQuery.class));
        verify(inboundService).createInbound(new MedicineInboundRequest(
                11L,
                21L,
                "LOT-001",
                request.expirationDate(),
                10,
                "신규 입고",
                TransactionType.INBOUND
        ));
    }

    @Test
    @DisplayName("선택한 재고 ID로 출고를 처리한다")
    void outbound() {
        OutboundToolRequest request = outboundRequest();
        givenOutboundTarget();

        OutboundToolResponse response = operationService.outbound(request);

        assertTrue(response.success());
        assertEquals(OutboundReason.DISPENSING, response.reason());
        verify(outboundService).getOutboundTarget(75L);
        verify(outboundService).outbound(75L, new MedicineOutboundRequest(
                11L,
                3,
                21L,
                OutboundReason.DISPENSING,
                "처방 출고"
        ));
        verifyNoInteractions(inventoryRepository, accessService);
    }

    @Test
    @DisplayName("의약품 후보가 여러 개면 입고하지 않고 정확한 입력을 요청한다")
    void inboundWithMultiplePackageUnits() {
        given(inventoryRepository.findPackageUnitTargets(any(FindMedicinePackageUnitTargetQuery.class)))
                .willReturn(List.of(
                        new MedicinePackageUnitTargetRow(11L, "타이레놀정", "500mg 10정"),
                        new MedicinePackageUnitTargetRow(12L, "타이레놀정", "500mg 30정")
                ));

        InboundToolResponse response = operationService.inbound(inboundRequest());

        assertFalse(response.success());
        assertTrue(response.message().contains("여러 의약품 포장단위"));
        verify(accessService, never()).getAccessibleStorageIds();
        verify(inboundService, never()).createInbound(any(MedicineInboundRequest.class));
    }

    @Test
    @DisplayName("재고 ID가 없으면 조회하거나 출고하지 않는다")
    void outboundWithoutInventoryId() {
        OutboundToolRequest request = new OutboundToolRequest(
                null, 3, OutboundReason.DISPENSING, null
        );

        OutboundToolResponse response = operationService.outbound(request);

        assertFalse(response.success());
        assertEquals("출고할 LOT의 재고 ID는 필수입니다.", response.message());
        verifyNoInteractions(outboundService, inventoryRepository, accessService);
    }

    @Test
    @DisplayName("출고 수량이 유효하지 않으면 조회하거나 출고하지 않는다")
    void outboundWithInvalidRequest() {
        OutboundToolRequest request = new OutboundToolRequest(
                75L, 0, OutboundReason.DISPENSING, null
        );

        OutboundToolResponse response = operationService.outbound(request);

        assertFalse(response.success());
        assertEquals("출고 수량은 1개 이상이어야 합니다.", response.message());
        verifyNoInteractions(outboundService, inventoryRepository, accessService);
    }

    @Test
    @DisplayName("기존 출고 서비스의 업무 오류를 실패 응답으로 반환한다")
    void outboundWithDomainFailure() {
        givenOutboundTarget();
        MedicineOutboundRequest command = new MedicineOutboundRequest(
                11L, 3, 21L, OutboundReason.DISPENSING, "처방 출고"
        );
        willThrow(new InsufficientStockException())
                .given(outboundService)
                .outbound(75L, command);

        OutboundToolResponse response = operationService.outbound(outboundRequest());

        assertFalse(response.success());
        assertEquals("출고 가능 재고가 부족합니다.", response.message());
        verify(outboundService).outbound(75L, command);
        verifyNoMoreInteractions(outboundService);
    }

    private void givenTargets() {
        given(inventoryRepository.findPackageUnitTargets(any(FindMedicinePackageUnitTargetQuery.class)))
                .willReturn(List.of(new MedicinePackageUnitTargetRow(11L, "타이레놀정", "500mg")));
        given(accessService.getAccessibleStorageIds()).willReturn(List.of(1L));
        given(inventoryRepository.findZoneTargets(any(FindZoneTargetQuery.class)))
                .willReturn(List.of(new ZoneTargetRow(21L, "A창고", "1구역")));
    }

    private InboundToolRequest inboundRequest() {
        return new InboundToolRequest(
                null,
                " 타이레놀 ",
                " 500mg ",
                null,
                " A창고 ",
                " 1구역 ",
                " LOT-001 ",
                LocalDate.now().plusDays(30),
                10,
                TransactionType.INBOUND,
                "신규입고"
        );
    }

    private OutboundToolRequest outboundRequest() {
        return new OutboundToolRequest(
                75L,
                3,
                OutboundReason.DISPENSING,
                " 처방 출고 "
        );
    }

    private void givenOutboundTarget() {
        given(outboundService.getOutboundTarget(75L))
                .willReturn(new MedicineOutboundTargetResponse(
                        75L, 11L, "ITEM-001", "타이레놀정", "500mg",
                        20, 1L, "A창고", 21L, "1구역"
                ));
    }
}
