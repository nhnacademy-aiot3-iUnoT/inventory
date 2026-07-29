package com.nhnacademy.inventory.medicines.medicine.service;


import com.nhnacademy.inventory.medicines.medicine.client.MedicineApiClient;
import com.nhnacademy.inventory.medicines.medicine.dto.MedicineResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tools.jackson.databind.ObjectMapper;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicineApiServiceTest {

    @Mock
    private MedicineApiClient medicineApiClient;

    @Mock
    private MedicineSaveService medicineSaveService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MedicineApiService medicineApiService;


    @BeforeEach
    void setUp(){

        medicineApiService = new MedicineApiService(medicineApiClient,medicineSaveService,objectMapper);

    }


    @Test
    @DisplayName("1페이지 테스트")
    void savedAllMedicines() {

        String json = """
                {

                "body": {
                                "totalCount": 1,
                                "items": [
                                  {
                                    "ITEM_SEQ": "12345",
                                    "ITEM_NAME": "테스트약",
                                    "ENTP_NAME": "테스트제약",
                                    "STORAGE_METHOD": "실온보관",
                                    "VALID_TERM": "24개월",
                                    "PACK_UNIT": "500mL/병, 1000mL/병",
                                    "NARCOTIC_KIND_CODE": null
                                  }
                                ]
                
                    }

                }
                """;


        when(medicineApiClient.getJson(1,500)).thenReturn(json);
        medicineApiService.savedAllMedicines();


        ArgumentCaptor<List<MedicineResponse>> captor = ArgumentCaptor.forClass(List.class);
        verify(medicineSaveService,times(1)).saveMedicines(captor.capture());

        MedicineResponse medicineResponse = captor.getValue().getFirst();




        assertAll(
                () -> assertEquals(1,captor.getValue().size()),
                () -> assertEquals("12345",medicineResponse.itemCode()),
                () -> assertEquals("테스트약",medicineResponse.productName()),
                () -> assertEquals("테스트제약",medicineResponse.companyName()),
                () -> assertEquals("실온보관",medicineResponse.storageMethod()),
                () -> assertEquals("24개월",medicineResponse.validityPeriod()),
                () -> assertEquals(List.of("500mL/병", "1000mL/병"), medicineResponse.packageUnits()),
                () -> assertNull(medicineResponse.narcoticKindCode())

        );




    }


    @Test
    @DisplayName("여러 페이지 수 테스트")
    void pagesCount(){

        String json = """
                
                {
                "body": {
                     "totalCount": 1200,
                     "items": []
                }

                }
                """;

        when(medicineApiClient.getJson(1,500)).thenReturn(json);
        when(medicineApiClient.getJson(2,500)).thenReturn(json);
        when(medicineApiClient.getJson(3,500)).thenReturn(json);


        medicineApiService.savedAllMedicines();
        verify(medicineApiClient).getJson(1,500);
        verify(medicineApiClient).getJson(2,500);
        verify(medicineApiClient).getJson(3,500);

        verify(medicineSaveService,times(3)).saveMedicines(anyList());



    }


    @Test
    @DisplayName("모든 정보 Null 체크 및 포장 단위 null인 경우")
    void errorTest(){


        String json = """
                
                {
                
                    "body": {
               
                        "totalCount": 1,
                        "items": [
                        {
                            "ITEM_SEQ": null,
                            "ITEM_NAME": null,
                            "ENTP_NAME": null,
                            "STORAGE_METHOD": null,
                            "VALID_TERM": null,
                            "PACK_UNIT": null,
                            "NARCOTIC_KIND_CODE": null
                            }
                        ]
                
                    }

                
                }

                """;


        when(medicineApiClient.getJson(1,500)).thenReturn(json);
        medicineApiService.savedAllMedicines();

        ArgumentCaptor<List<MedicineResponse>> captor = ArgumentCaptor.captor();
        verify(medicineSaveService).saveMedicines(captor.capture());

        MedicineResponse response = captor.getValue().getFirst();

        assertAll(

                () -> assertNull(response.itemCode()),
                () -> assertNull(response.productName()),
                () -> assertNull(response.companyName()),
                () -> assertNull(response.storageMethod()),
                () -> assertEquals(List.of("포장단위 정보 없음"),response.packageUnits()),
                () -> assertNull(response.validityPeriod()),
                () -> assertNull(response.narcoticKindCode()),
                () -> assertDoesNotThrow(()-> medicineApiService.savedAllMedicines())



        );



    }



    @Test
    @DisplayName("포장단위 수출용 무시 및 파싱 체크")
    void packOutput(){

        String json = """
                
                {
                
                    "body": {
               
                        "totalCount": 1,
                        "items": [
                        {
                            "ITEM_SEQ": "12345",
                            "ITEM_NAME": "테스트약",
                            "ENTP_NAME": "테스트제약",
                            "STORAGE_METHOD": "실온보관",
                            "VALID_TERM": "24개월",
                            "PACK_UNIT": "수출용 500mL/병, 1000mL/병",
                            "NARCOTIC_KIND_CODE": null
                            }
                        ]
                
                    }

                
                }

                """;


        when(medicineApiClient.getJson(1,500)).thenReturn(json);
        medicineApiService.savedAllMedicines();

        ArgumentCaptor<List<MedicineResponse>> captor = ArgumentCaptor.captor();
        verify(medicineSaveService).saveMedicines(captor.capture());

        MedicineResponse response = captor.getValue().getFirst();

        assertNotNull(response.packageUnits().getFirst());
        assertEquals("1000mL/병",response.packageUnits().getFirst());



    }







}