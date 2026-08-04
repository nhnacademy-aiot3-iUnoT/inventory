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
    @DisplayName("포장단위가 없는 경우")
    void packUnitTest(){

        String json = """

                {
                    "body": {
                        "totalCount": 1,
                        "items": [
                        {




                            }
                        ]



                    }



                }


                """;




    }






}