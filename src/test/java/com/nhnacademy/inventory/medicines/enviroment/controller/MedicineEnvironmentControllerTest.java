package com.nhnacademy.inventory.medicines.enviroment.controller;

import com.nhnacademy.inventory.medicines.enviroment.domain.EnvironmentType;

import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentRequest;
import com.nhnacademy.inventory.medicines.enviroment.dto.MedicineEnvironmentTypeResponse;
import com.nhnacademy.inventory.medicines.enviroment.service.EnvironmentTypeSearchService;
import com.nhnacademy.inventory.medicines.enviroment.service.MedicineEnvironmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;


import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicineEnvironmentController.class)
class MedicineEnvironmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EnvironmentTypeSearchService environmentTypeSearchService;
    @MockitoBean
    MedicineEnvironmentService medicineEnvironmentService;

    @Test
    @DisplayName("환경기준 조회")
    void getEnvironmentTypes() throws Exception{


        MedicineEnvironmentTypeResponse response = new MedicineEnvironmentTypeResponse(
                EnvironmentType.TEMPERATURE,
                new BigDecimal("1"),
                new BigDecimal("30")
        );


        given(environmentTypeSearchService.getTypes(1L)).willReturn(List.of(response));

        mockMvc.perform(get("/api/core/medicine-environment/package-units/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
          
                {
                    "success": true,
                    "data": [
                        {
                          "type": "TEMPERATURE",
                          "min" : 1,
                          "max" : 30
                        }
                    ],
                    "error": null

                }

                """));


        verify(environmentTypeSearchService).getTypes(1L);

    }

    @Test
    @DisplayName("환경기준 수정")
    void updateEnvironmentTypes() throws Exception{


        MedicineEnvironmentRequest request = new MedicineEnvironmentRequest(
                new BigDecimal("10"),
                new BigDecimal("20"),
                new BigDecimal("10"),
                new BigDecimal("30"),
                new BigDecimal("10"),
                new BigDecimal("50")

        );

        mockMvc.perform(put("/api/core/medicine-environment/package-units/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                
                                {
                                    "minTemperature": 10,
                                    "maxTemperature": 20,
                                    "minHumidity": 10,
                                    "maxHumidity": 30,
                                    "minIlluminance": 10,
                                    "maxIlluminance": 50
                                
                                }

                                """))
                .andExpect(status().isNoContent());

        verify(medicineEnvironmentService).updateTypes(1L,request);

    }



    @Test
    @DisplayName("환경기준 삭제")
    void deleteEnvironmentTypes() throws Exception{

        mockMvc.perform(delete("/api/core/medicine-environment/package-units/1"))
                .andExpect(status().isNoContent());


        verify(medicineEnvironmentService).deleteTypes(1L);


    }


    @Test
    @DisplayName("request validation 체크")
    void validationTest() throws Exception{

        // min > max
        mockMvc.perform(put("/api/core/medicine-environment/package-units/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                       
                        {
                            "minTemperature": 30,
                            "maxTemperature": 10,
                            "minHumidity": 10,
                            "maxHumidity": 30,
                            "minIlluminance": 10,
                            "maxIlluminance": 50
                      
                       }

                       """)


        ).andExpect(status().is4xxClientError());


        mockMvc.perform(put("/api/core/medicine-environment/package-units/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                       
                        {
                            "minTemperature": 10,
                            "maxTemperature": 30,
                            "minHumidity": 30,
                            "maxHumidity": 10,
                            "minIlluminance": 10,
                            "maxIlluminance": 50
                      
                       }

                       """)


        ).andExpect(status().is4xxClientError());



        mockMvc.perform(put("/api/core/medicine-environment/package-units/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                       
                        {
                            "minTemperature": 10,
                            "maxTemperature": 30,
                            "minHumidity": 10,
                            "maxHumidity": 30,
                            "minIlluminance": 50,
                            "maxIlluminance": 10
                      
                       }

                       """)


        ).andExpect(status().is4xxClientError());

        verify(medicineEnvironmentService,never()).updateTypes(anyLong(),any(MedicineEnvironmentRequest.class));



    }


    @Test
    @DisplayName("validation null 검증")
    void nullTest() throws Exception{


        mockMvc.perform(put("/api/core/medicine-environment/package-units/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                
                                {
                                    "minTemperature": null,
                                    "maxTemperature": 20,
                                    "minHumidity": 10,
                                    "maxHumidity": 30,
                                    "minIlluminance": 10,
                                    "maxIlluminance": 50
                                
                                }

                                """))
                .andExpect(status().is4xxClientError());



        mockMvc.perform(put("/api/core/medicine-environment/package-units/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                
                                {
                                    "minTemperature": 10,
                                    "maxTemperature": 20,
                                    "minHumidity": 10,
                                    "maxHumidity": null,
                                    "minIlluminance": 10,
                                    "maxIlluminance": 50
                                
                                }

                                """))
                .andExpect(status().is4xxClientError());



        mockMvc.perform(put("/api/core/medicine-environment/package-units/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                
                                {
                                    "minTemperature": 10,
                                    "maxTemperature": 20,
                                    "minHumidity": 10,
                                    "maxHumidity": 30,
                                    "minIlluminance": 10,
                                    "maxIlluminance": null
                                
                                }

                                """))
                .andExpect(status().is4xxClientError());


        verify(medicineEnvironmentService,never()).updateTypes(anyLong(),any(MedicineEnvironmentRequest.class));


    }










}