package com.nhnacademy.inventory.inventories.inventory.controller;

import com.netflix.discovery.converters.Auto;
import com.nhnacademy.inventory.global.dto.PageResponse;
import com.nhnacademy.inventory.inventories.inventory.domain.ManagementStatus;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoriesResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoryDetailResponse;
import com.nhnacademy.inventory.inventories.inventory.dto.InventoryInfoResponse;
import com.nhnacademy.inventory.inventories.inventory.service.InventoriesSearchService;
import com.nhnacademy.inventory.inventories.inventory.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {

    @Autowired
    MockMvc mockMvc;
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
                .andExpect(jsonPath("$.data.content[0].totalQuantity").value("30"));



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


        mockMvc.perform(get("/api/core/inventories/storages/1/pack-units/1"))
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

        """));




    }


}
