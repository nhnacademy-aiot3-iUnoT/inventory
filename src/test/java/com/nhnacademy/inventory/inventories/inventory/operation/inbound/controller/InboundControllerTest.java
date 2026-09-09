package com.nhnacademy.inventory.inventories.inventory.operation.inbound.controller;

import com.nhnacademy.inventory.inventories.inventory.operation.inbound.dto.MedicineInboundRequest;
import com.nhnacademy.inventory.inventories.inventory.operation.inbound.service.InboundService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(InboundController.class)
class InboundControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    InboundService inboundService;



    @Test
    @DisplayName("입고 등록 요청")
    void registerTest() throws Exception{


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
                        "transactionType": "INBOUND"
                     
                        }
                        
                        """
                )).andExpect(status().isCreated());

        verify(inboundService).createInbound(any(MedicineInboundRequest.class));

    }



    @Test
    @DisplayName("validation")
    void validationInventoryTest() throws Exception{


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
                            "transactionType": "INBOUND"
     
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
                            "transactionType": "INBOUND"
     
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
                            "transactionType": "INBOUND"
     
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
                            "transactionType": "INBOUND"
     
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
                            "transactionType": "INBOUND"
     
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
                            "transactionType": "INBOUND"
     
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
                            "transactionType": "INBOUND"
     
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
                            "transactionType": "INBOUND"
     
                        }
                        """
                )).andExpect(status().is4xxClientError());





    }



}
