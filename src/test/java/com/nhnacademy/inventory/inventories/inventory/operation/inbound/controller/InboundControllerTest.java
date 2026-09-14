package com.nhnacademy.inventory.inventories.inventory.operation.inbound.controller;

import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.MedicineInboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.service.InboundService;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;


@WebMvcTest(InboundController.class)
class InboundControllerTest extends SupportControllerTest {

    @MockitoBean
    InboundService inboundService;


    @Test
    @DisplayName("입고 등록 요청")
    void registerTest() throws Exception {


        mockMvc.perform(post("/api/core/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        
                        "medicinePackageUnitId": 1,
                        "zoneId": 1,
                        "lotNumber": "ABC-123",
                        "expirationDate": "2027-09-10",
                        "quantity": 20,
                        "memo": null,
                        "transactionType": "INBOUND",
                        "overwriteExpirationDate": false
                        }
                        
                        """
                )).andExpect(status().isCreated())
                .andDo(document("inventory-inbound-create",
                        requestFields(
                                fieldWithPath("medicinePackageUnitId").description("의약품 포장 단위 ID"),
                                fieldWithPath("zoneId").description("입고 구역 ID"),
                                fieldWithPath("lotNumber").description("제조번호"),
                                fieldWithPath("expirationDate").description("유통기한"),
                                fieldWithPath("quantity").description("입고 수량"),
                                fieldWithPath("memo").description("메모").optional(),
                                fieldWithPath("transactionType").description("거래 유형"),
                                fieldWithPath("overwriteExpirationDate").description("유통기한 정정 여부")
                        )
                ));

        verify(inboundService).createInbound(any(MedicineInboundRequest.class));

    }


    @Test
    @DisplayName("validation")
    void validationInventoryTest() throws Exception {


        // medicinePackageUnitId가 null일 때
        mockMvc.perform(post("/api/core/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                    "medicinePackageUnitId": null,
                                    "zoneId": 1,
                                    "lotNumber": "ABC-123",
                                    "expirationDate": "2026-08-11",
                                    "quantity": 20,
                                    "memo": null,
                                    "transactionType": "INBOUND",
                                    "overwriteExpirationDate": false
                                }
                                """
                )).andExpect(status().is4xxClientError());


        // zoneId가 Null일 때

        mockMvc.perform(post("/api/core/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                    "medicinePackageUnitId": 1,
                                    "zoneId": null,
                                    "lotNumber": "ABC-123",
                                    "expirationDate": "2026-08-11",
                                    "quantity": 20,
                                    "memo": null,
                                    "transactionType": "INBOUND",
                                    "overwriteExpirationDate": false
                                }
                                """
                )).andExpect(status().is4xxClientError());


        // lotNumber 공백

        mockMvc.perform(post("/api/core/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                    "medicinePackageUnitId": 1,
                                    "zoneId": 1,
                                    "lotNumber": " ",
                                    "expirationDate": "2026-08-11",
                                    "quantity": 20,
                                    "memo": null,
                                    "transactionType": "INBOUND",
                                    "overwriteExpirationDate": false
                                }
                                """
                )).andExpect(status().is4xxClientError());


        // lotNumber 50자 이상

        mockMvc.perform(post("/api/core/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                    "medicinePackageUnitId": 1,
                                    "zoneId": 1,
                                    "lotNumber": "아러더랴더ㅑㄹ더잳쟈러재댜ㅓ랮댜ㅓ래쟏러ㅐㅑㅈ덜댜잗러랒대랴ㅓ잳러ㅐ쟈더래쟈더래ㅑㅈ더갸ㅐㅈ더래ㅑㅈ더랴ㅐㅈ더랴ㅐ젇랴ㅐㅈ덪ㄷ",
                                    "expirationDate": "2026-08-11",
                                    "quantity": 20,
                                    "memo": null,
                                    "transactionType": "INBOUND",
                                    "overwriteExpirationDate": false
                                }
                                """
                )).andExpect(status().is4xxClientError());


        // expirationDate null일 때

        mockMvc.perform(post("/api/core/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                    "medicinePackageUnitId": 1,
                                    "zoneId": 1,
                                    "lotNumber": " ",
                                    "expirationDate": null,
                                    "quantity": 20,
                                    "memo": null,
                                    "transactionType": "INBOUND",
                                    "overwriteExpirationDate": false
                                }
                                """
                )).andExpect(status().is4xxClientError());


        // expirationDate 과거일 때

        mockMvc.perform(post("/api/core/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                    "medicinePackageUnitId": 1,
                                    "zoneId": 1,
                                    "lotNumber": "ABC-123" ,
                                    "expirationDate": "2025-05-10",
                                    "quantity": 20,
                                    "memo": null,
                                    "transactionType": "INBOUND",
                                    "overwriteExpirationDate": false
                                }
                                """
                )).andExpect(status().is4xxClientError());


        // 수량이 null 일 때

        mockMvc.perform(post("/api/core/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                    "medicinePackageUnitId": 1,
                                    "zoneId": 1,
                                    "lotNumber": "ABC-123",
                                    "expirationDate": "2026-08-11",
                                    "quantity": null,
                                    "memo": null,
                                    "transactionType": "INBOUND",
                                    "overwriteExpirationDate": false
                                }
                                """
                )).andExpect(status().is4xxClientError());


        // 수량이 음수일 때


        mockMvc.perform(post("/api/core/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        """
                                {
                                    "medicinePackageUnitId": 1,
                                    "zoneId": 1,
                                    "lotNumber": "ABC-123",
                                    "expirationDate": "2026-08-11",
                                    "quantity": -30,
                                    "memo": null,
                                    "transactionType": "INBOUND",
                                    "overwriteExpirationDate": false
                                }
                                """
                )).andExpect(status().is4xxClientError());


    }


}
