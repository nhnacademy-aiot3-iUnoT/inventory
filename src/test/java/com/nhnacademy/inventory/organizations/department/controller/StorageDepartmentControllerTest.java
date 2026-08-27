package com.nhnacademy.inventory.organizations.department.controller;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.department.domain.DepartmentStatus;
import com.nhnacademy.inventory.organizations.department.dto.response.StorageByDepartmentResponse;
import com.nhnacademy.inventory.organizations.department.service.StorageDepartmentService;
import com.nhnacademy.inventory.organizations.storage.domain.StorageStatus;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StorageDepartmentController.class)
class StorageDepartmentControllerTest extends SupportControllerTest {
    @MockitoBean
    private StorageDepartmentService storageDepartmentService;

    @Nested
    @DisplayName("부서별 저장소 조회 GET /api/core/departments/{department-id}/storages")
    class GetStoragesByDepartment {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            given(storageDepartmentService.getStoragesByDepartmentId(1L)).willReturn(List.of(
                    new StorageByDepartmentResponse(2L, "본관 저장소", StorageStatus.ACTIVE)));

            mockMvc.perform(get("/api/core/departments/1/storages"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].storageId").value(2L));
        }

        @Test
        @DisplayName("성공 - 결과 없음")
        void empty() throws Exception {
            given(storageDepartmentService.getStoragesByDepartmentId(1L)).willReturn(List.of());

            mockMvc.perform(get("/api/core/departments/1/storages"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(storageDepartmentService).getStoragesByDepartmentId(1L);

            mockMvc.perform(get("/api/core/departments/1/storages"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("저장소별 부서 조회 GET /api/core/storages/{storage-id}/departments")
    class GetDepartmentsByStorage {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            given(storageDepartmentService.getDepartmentsByStorageId(1L)).willReturn(List.of(
                    new DepartmentByStorageResponse(2L, "이비인후과", DepartmentStatus.ACTIVE)));

            mockMvc.perform(get("/api/core/storages/1/departments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].departmentId").value(2L));
        }

        @Test
        @DisplayName("성공 - 결과 없음")
        void empty() throws Exception {
            given(storageDepartmentService.getDepartmentsByStorageId(1L)).willReturn(List.of());

            mockMvc.perform(get("/api/core/storages/1/departments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("부서 저장소 연결 POST/DELETE /api/core/departments/{department-id}/storages/{storage-id}")
    class Relation {
        @Test
        @DisplayName("연결 성공")
        void addSuccess() throws Exception {
            mockMvc.perform(post("/api/core/departments/1/storages/2"))
                    .andExpect(status().isNoContent());

            verify(storageDepartmentService).addStorage(1L, 2L);
        }

        @Test
        @DisplayName("연결 실패 - 권한 없음")
        void addForbidden() throws Exception {
            willThrow(new ForbiddenException()).given(storageDepartmentService).addStorage(1L, 2L);

            mockMvc.perform(post("/api/core/departments/1/storages/2"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("연결 해제 성공")
        void removeSuccess() throws Exception {
            mockMvc.perform(delete("/api/core/departments/1/storages/2"))
                    .andExpect(status().isNoContent());

            verify(storageDepartmentService).removeStorage(1L, 2L);
        }

        @Test
        @DisplayName("연결 해제 실패 - 권한 없음")
        void removeForbidden() throws Exception {
            willThrow(new ForbiddenException()).given(storageDepartmentService).removeStorage(1L, 2L);

            mockMvc.perform(delete("/api/core/departments/1/storages/2"))
                    .andExpect(status().isForbidden());
        }
    }
}
