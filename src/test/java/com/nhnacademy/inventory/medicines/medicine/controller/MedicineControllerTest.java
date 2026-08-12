package com.nhnacademy.inventory.medicines.medicine.controller;


import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageDetailResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicinePackageSearchResponse;
import com.nhnacademy.inventory.medicines.medicine.dto.request.MedicineSearchRequest;
import com.nhnacademy.inventory.medicines.medicine.service.MedicineApiService;
import com.nhnacademy.inventory.medicines.medicine.service.MedicineSearchService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicineController.class)
class MedicineControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MedicineApiService medicineApiService;

    @MockitoBean
    MedicineSearchService medicineSearchService;



//    @Test
//    void savedMedicines() throws Exception{
//
//
//        mockMvc.perform(post("/api/core/medicines/admin/import"))
//                .andExpect(status().isNoContent());
//
//
//        verify(medicineApiService).savedAllMedicines();
//
//
//    }

    @Test
    @DisplayName("의약품 정보 조회")
    void getMedicinesTest() throws Exception{

        Pageable pageable = Pageable.ofSize(10);

        Page<MedicinePackageSearchResponse> result = new PageImpl<>(
                List.of(new MedicinePackageSearchResponse(
                        1L,
                        1L,
                        "123456789",
                        "product-test-name",
                        "company-test",
                        "10ml"
                )),pageable,1

        );


        // 컨트롤러 테스트에서는 보통 any()를 사용
        // 서비스의 내부 인자 비교에 테스트가 너무 묶이지 않게 만들기도 함.

        given(medicineSearchService.getMedicines(any(MedicineSearchRequest.class),any(Pageable.class))).willReturn(result);


        mockMvc.perform(get("/api/core/medicines")
                        .param("searchType","PRODUCT_NAME")
                        .param("search","product-test")
                        .param("page","0")
                        .param("size","10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(

                        """
                    {
                         "success": true,
                         "data": {
                            "content" : [
                                {
                                    "medicineId":1,
                                    "packageUnitId":1,
                                    "itemCode":"123456789",
                                    "productName":"product-test-name",
                                    "companyName":"company-test",
                                    "packUnit":"10ml"
                                }
                            ],
                            "page" : 0,
                            "size" : 10,
                            "totalElements" : 1,
                            "totalPages" : 1,
                            "last" : true

                         },
                         "error" : null


                    }


                """

                ));

            verify(medicineSearchService).getMedicines(any(MedicineSearchRequest.class),any(Pageable.class));



    }


    @Test
    @DisplayName("특정 의약품 조회")
    void getDetailMedicineTest() throws Exception{


        MedicinePackageDetailResponse response = new MedicinePackageDetailResponse(
                1L,
                1L,
                "123456789",
                "product-test-name",
                "company-test",
                "storage-test",
                "validity-test",
                "10ml",
                null
        );

        given(medicineSearchService.getDetail(anyLong())).willReturn(response);
        mockMvc.perform(
                get("/api/core/medicines/package-units/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                
                {
                    "success": true,
                    "data": {
                        "medicineId": 1,
                        "packageUnitId": 1,
                        "itemCode": "123456789",
                        "productName": "product-test-name",
                        "companyName": "company-test",
                        "storageMethod": "storage-test",
                        "validityPeriod": "validity-test",
                        "packUnit": "10ml",
                        "narcoticKindCode": null

                    },
                    "error": null

                }

"""));

    verify(medicineSearchService).getDetail(anyLong());


    }














}