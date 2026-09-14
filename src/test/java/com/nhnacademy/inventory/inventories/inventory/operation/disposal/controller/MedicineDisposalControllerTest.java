package com.nhnacademy.inventory.inventories.inventory.operation.disposal.controller;

import com.nhnacademy.inventory.inventories.inventory.operation.disposal.service.MedicineDisposalService;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MedicineDisposalController.class)
class MedicineDisposalControllerTest extends SupportControllerTest {
    @MockitoBean
    MedicineDisposalService medicineDisposalService;

    @Test
    void disposeExpiredInventoriesSuccess() throws Exception {
        mockMvc.perform(post("/api/core/inventories/expired-disposals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"inventoryIds\":[1,2,3]}"))
                .andExpect(status().isNoContent())
                .andDo(document("inventory-expired-disposal",
                        requestFields(
                                fieldWithPath("inventoryIds").description("폐기할 만료 재고 ID 목록")
                        )));
    }
}
