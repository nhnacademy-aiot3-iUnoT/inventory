package com.nhnacademy.inventory.medicines.medicine.controller;

import com.nhnacademy.inventory.medicines.medicine.service.MedicineApiService;
import com.nhnacademy.inventory.medicines.medicine.service.MedicineSaveService;
import com.nhnacademy.inventory.medicines.medicine.service.MedicineSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;



import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void savedMedicines() throws Exception{


        mockMvc.perform(post("/api/core/admin/medicines/import"))
                .andExpect(status().isNoContent());

    


        verify(medicineApiService).savedAllMedicines();


    }
}