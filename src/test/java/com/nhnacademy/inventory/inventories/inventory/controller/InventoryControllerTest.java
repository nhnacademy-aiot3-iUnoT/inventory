package com.nhnacademy.inventory.inventories.inventory.controller;

import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoryDetailResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoryInfoResponse;
import com.nhnacademy.inventory.inventories.inventory.service.InventoriesSearchService;
import com.nhnacademy.inventory.inventories.inventory.service.InventoryService;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest extends SupportControllerTest {

    @MockitoBean
    InventoriesSearchService inventoriesSearchService;
    @MockitoBean
    InventoryService inventoryService;



    @Test
    @DisplayName("전체 입고 조회 테스트")
    void getInventoriesTest() throws Exception{


        InventoriesResponse response = new InventoriesResponse(
                1L,
                1L,
                "타이레놀",
                "1234",
                "10통",
                LocalDate.of(2026,9,1),
                "저장소-test",
                30
        );


        given(inventoriesSearchService.getInventories(
                eq("타이레놀"),
                eq(1L),
                any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(response),Pageable.ofSize(10),1));

        mockMvc.perform(
                        get("/api/core/inventories")
                                .param("search","타이레놀")
                                .param("storage-id","1")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "expirationDate,asc")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.content[0].storageId").value(1L))
                .andExpect(jsonPath("$.data.content[0].packUnitId").value(1L))
                .andExpect(jsonPath("$.data.content[0].productName").value("타이레놀"))
                .andExpect(jsonPath("$.data.content[0].itemCode").value("1234"))
                .andExpect(jsonPath("$.data.content[0].packUnit").value("10통"))
                .andExpect(jsonPath("$.data.content[0].expirationDate").value("2026-09-01"))
                .andExpect(jsonPath("$.data.content[0].storageName").value("저장소-test"))
                .andExpect(jsonPath("$.data.content[0].totalQuantity").value(30))
                .andDo(document("inventory-get-list",
                        queryParameters(
                                parameterWithName("search").description("의약품명 검색어").optional(),
                                parameterWithName("storage-id").description("저장소 ID").optional(),
                                parameterWithName("page").description("페이지 번호(0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 크기").optional(),
                                parameterWithName("sort").description("정렬 기준").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.content[].storageId").description("저장소 ID"),
                                fieldWithPath("data.content[].packUnitId").description("의약품 포장 단위 ID"),
                                fieldWithPath("data.content[].productName").description("의약품명"),
                                fieldWithPath("data.content[].itemCode").description("품목 코드"),
                                fieldWithPath("data.content[].packUnit").description("포장 단위"),
                                fieldWithPath("data.content[].expirationDate").description("유통기한"),
                                fieldWithPath("data.content[].storageName").description("저장소명"),
                                fieldWithPath("data.content[].totalQuantity").description("총 재고 수량"),
                                fieldWithPath("data.page").description("현재 페이지"),
                                fieldWithPath("data.size").description("페이지 크기"),
                                fieldWithPath("data.totalElements").description("전체 요소 수"),
                                fieldWithPath("data.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.last").description("마지막 페이지 여부"),
                                fieldWithPath("error").description("오류 정보(성공 시 null)"),
                                fieldWithPath("timestamp").description("응답 생성 시각")
                        )
                ));



    }


    @Test
    @DisplayName("상세 재고 조회")
    void getInventoryInfo() throws Exception{


        List<InventoryDetailResponse> details = List.of(
                new InventoryDetailResponse(
                        1L,
                        1L,
                        "A 구역",
                        "lot-3452",
                        LocalDate.of(2026, Month.FEBRUARY,24),
                        50,
                        ManagementStatus.NORMAL

                )
        );


        InventoryInfoResponse info = new InventoryInfoResponse(
                1L,
                1L,
                "A 저장소",
                "ABC-123",
                "테스트 약",
                PageResponse.from(new PageImpl<>(details))
        );

        given(inventoryService.getInventoryInfo(eq(1L),eq(1L),any(Pageable.class)))
                .willReturn(info);


        mockMvc.perform(get("/api/core/inventories/storages/{storage-id}/pack-units/{medicine-package-unit-id}",
                        1L, 1L)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "expirationDate,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""

                {

                    "success": true,
                    "data" : {

                         "medicinePackUnitId": 1,
                         "storageId": 1,
                         "storageName": "A 저장소",
                         "itemCode": "ABC-123",
                         "productName": "테스트 약",
                         "inventories":
                            {
                              "content" : [
                            {
                                 "inventoryId": 1,
                                 "zoneId": 1,
                                 "zoneName": "A 구역",
                                 "lotNumber": "lot-3452",
                                 "expirationDate": "2026-02-24",
                                 "currentQuantity": 50,
                                 "managementStatus":"NORMAL"

                            }
   
                         ],
                            "page": 0,
                            "size": 1,
                            "totalElements": 1,
                            "totalPages": 1,
                            "last": true


                            }

  


                    },
                  "error":null

                }

        """))
                .andDo(document("inventory-get-detail",
                        pathParameters(
                                parameterWithName("storage-id").description("저장소 ID"),
                                parameterWithName("medicine-package-unit-id").description("의약품 포장 단위 ID")
                        ),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호(0부터 시작)").optional(),
                                parameterWithName("size").description("페이지 크기").optional(),
                                parameterWithName("sort").description("정렬 기준").optional()
                        ),
                        responseFields(
                                fieldWithPath("success").description("성공 여부"),
                                fieldWithPath("data.medicinePackUnitId").description("의약품 포장 단위 ID"),
                                fieldWithPath("data.storageId").description("저장소 ID"),
                                fieldWithPath("data.storageName").description("저장소명"),
                                fieldWithPath("data.itemCode").description("품목 코드"),
                                fieldWithPath("data.productName").description("의약품명"),
                                fieldWithPath("data.inventories.content[].inventoryId").description("재고 ID"),
                                fieldWithPath("data.inventories.content[].zoneId").description("구역 ID"),
                                fieldWithPath("data.inventories.content[].zoneName").description("구역명"),
                                fieldWithPath("data.inventories.content[].lotNumber").description("제조번호"),
                                fieldWithPath("data.inventories.content[].expirationDate").description("유통기한"),
                                fieldWithPath("data.inventories.content[].currentQuantity").description("현재 수량"),
                                fieldWithPath("data.inventories.content[].managementStatus").description("재고 관리 상태"),
                                fieldWithPath("data.inventories.page").description("현재 페이지"),
                                fieldWithPath("data.inventories.size").description("페이지 크기"),
                                fieldWithPath("data.inventories.totalElements").description("전체 요소 수"),
                                fieldWithPath("data.inventories.totalPages").description("전체 페이지 수"),
                                fieldWithPath("data.inventories.last").description("마지막 페이지 여부"),
                                fieldWithPath("error").description("오류 정보(성공 시 null)"),
                                fieldWithPath("timestamp").description("응답 생성 시각")
                        )
                ));




    }


}
