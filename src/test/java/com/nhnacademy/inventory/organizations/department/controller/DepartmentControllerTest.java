package com.nhnacademy.inventory.organizations.department.controller;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.department.domain.DepartmentStatus;
import com.nhnacademy.inventory.organizations.department.dto.request.DepartmentCreateRequest;
import com.nhnacademy.inventory.organizations.department.dto.request.DepartmentStatusUpdateRequest;
import com.nhnacademy.inventory.organizations.department.dto.request.DepartmentUpdateRequest;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentCreateResponse;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentInfoResponse;
import com.nhnacademy.inventory.organizations.department.dto.response.DepartmentListResponse;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentAlreadyExistsException;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.service.DepartmentService;
import com.nhnacademy.inventory.organizations.department.service.MemberDepartmentService;
import com.nhnacademy.inventory.support.RestDocsUtils;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
class DepartmentControllerTest extends SupportControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DepartmentService departmentService;

    @MockitoBean
    private MemberDepartmentService memberDepartmentService;

    @Nested
    @DisplayName("부서 생성 POST /api/core/departments")
    class createDepartment {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            DepartmentCreateRequest request = new DepartmentCreateRequest("이비인후과", "부서 설명");

            DepartmentCreateResponse response = new DepartmentCreateResponse(1L, "이비인후과");

            given(departmentService.createDepartment(request)).willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());

            responseFields.addAll(
                    List.of(
                            fieldWithPath("data.id").type(NUMBER).description("부서 ID"),
                            fieldWithPath("data.name").type(STRING).description("부서 이름")
                    )
            );

            mockMvc.perform(post("/api/core/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("이비인후과"))
                    .andDo(document("department-create",
                            requestFields(
                                    fieldWithPath("name").type(STRING).description("부서 이름"),
                                    fieldWithPath("description").type(STRING).description("부서 설명").optional()),
                            responseFields(responseFields)));

            verify(departmentService).createDepartment(request);
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void invalidInput() throws Exception {
            DepartmentCreateRequest request = new DepartmentCreateRequest("", "");

            mockMvc.perform(post("/api/core/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            DepartmentCreateRequest request = new DepartmentCreateRequest("이비인후과", "부서 설명");

            given(departmentService.createDepartment(request)).willThrow(new ForbiddenException());

            mockMvc.perform(post("/api/core/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 부서 이름 중복")
        void duplicate() throws Exception {
            DepartmentCreateRequest request = new DepartmentCreateRequest("이비인후과", "부서 설명");

            given(departmentService.createDepartment(request)).willThrow(new DepartmentAlreadyExistsException());

            mockMvc.perform(post("/api/core/departments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("전체 부서 목록 조회 GET /api/core/departments")
    class getDepartments {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            List<DepartmentListResponse> responses = List.of(
                    new DepartmentListResponse(1L, "이비인후과", DepartmentStatus.ACTIVE),
                    new DepartmentListResponse(2L, "정형외과", DepartmentStatus.INACTIVE));

            given(departmentService.getDepartments()).willReturn(responses);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());

            responseFields.addAll(
                    List.of(
                            fieldWithPath("data[].id").type(NUMBER).description("부서 ID"),
                            fieldWithPath("data[].name").type(STRING).description("부서 이름"),
                            fieldWithPath("data[].status").type(STRING).description("부서 상태")
                    )
            );

            mockMvc.perform(get("/api/core/departments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("이비인후과"))
                    .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                    .andDo(document("department-list",
                            responseFields(responseFields)));

            verify(departmentService).getDepartments();
        }

        @Test
        @DisplayName("성공 - 조회 결과 없음")
        void empty() throws Exception {
            given(departmentService.getDepartments()).willReturn(List.of());

            mockMvc.perform(get("/api/core/departments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("본인 부서 목록 조회 GET /api/core/departments/me")
    class getMyDepartments {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            List<DepartmentListResponse> responses = List.of(
                    new DepartmentListResponse(1L, "이비인후과", DepartmentStatus.ACTIVE),
                    new DepartmentListResponse(2L, "정형외과", DepartmentStatus.INACTIVE));

            given(memberDepartmentService.getMyDepartments()).willReturn(responses);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());

            responseFields.addAll(
                    List.of(
                            fieldWithPath("data[].id").type(NUMBER).description("부서 ID"),
                            fieldWithPath("data[].name").type(STRING).description("부서 이름"),
                            fieldWithPath("data[].status").type(STRING).description("부서 상태")
                    )
            );

            mockMvc.perform(get("/api/core/departments/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value(1))
                    .andExpect(jsonPath("$.data[0].name").value("이비인후과"))
                    .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                    .andDo(document("my-department-list",
                            responseFields(responseFields)));

            verify(memberDepartmentService).getMyDepartments();
        }

        @Test
        @DisplayName("성공 - 조회 결과 없음")
        void empty() throws Exception {
            given(memberDepartmentService.getMyDepartments()).willReturn(List.of());

            mockMvc.perform(get("/api/core/departments/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("부서 단건 조회 GET /api/core/departments/{department-id}")
    class getDepartment {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            DepartmentInfoResponse response = new DepartmentInfoResponse(
                    1L, "이비인후과",
                    "부서 설명", DepartmentStatus.ACTIVE, LocalDateTime.now()
            );

            given(departmentService.getDepartment(1L)).willReturn(response);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(departmentInfoResponseFields());

            mockMvc.perform(get("/api/core/departments/{department-id}", 1L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("이비인후과"))
                    .andDo(document("department-get",
                            pathParameters(parameterWithName("department-id").description("부서 ID")),
                            responseFields(responseFields)));

            verify(departmentService).getDepartment(1L);
        }

        @Test
        @DisplayName("실패 - 부서 없음")
        void notFound() throws Exception {
            given(departmentService.getDepartment(1L)).willThrow(new DepartmentNotFoundException());

            mockMvc.perform(get("/api/core/departments/{department-id}", 1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("부서 상태 변경 PUT /api/core/departments/{department-id}/status")
    class updateDepartmentStatus {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            DepartmentStatusUpdateRequest request = new DepartmentStatusUpdateRequest(DepartmentStatus.INACTIVE);

            mockMvc.perform(put("/api/core/departments/{department-id}/status", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent())
                    .andDo(document("department-status-update",
                            pathParameters(parameterWithName("department-id").description("부서 ID")),
                            requestFields(fieldWithPath("status").type(STRING).description("부서 상태"))
                    ));

            verify(departmentService).updateDepartmentStatus(request, 1L);
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void invalidInput() throws Exception {
            DepartmentStatusUpdateRequest request = new DepartmentStatusUpdateRequest(null);

            mockMvc.perform(put("/api/core/departments/{department-id}/status", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            DepartmentStatusUpdateRequest request = new DepartmentStatusUpdateRequest(DepartmentStatus.INACTIVE);

            given(departmentService.updateDepartmentStatus(request, 1L)).willThrow(new ForbiddenException());

            mockMvc.perform(put("/api/core/departments/{department-id}/status", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 부서 없음")
        void notFound() throws Exception {
            DepartmentStatusUpdateRequest request = new DepartmentStatusUpdateRequest(DepartmentStatus.INACTIVE);

            given(departmentService.updateDepartmentStatus(request, 1L)).willThrow(new DepartmentNotFoundException());

            mockMvc.perform(put("/api/core/departments/{department-id}/status", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("부서 정보 수정 PUT /api/core/departments/{department-id}")
    class updateDepartment {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            DepartmentUpdateRequest request = new DepartmentUpdateRequest("정형외과", "기획 부서");

            mockMvc.perform(put("/api/core/departments/{department-id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent())
                    .andDo(document("department-update",
                            pathParameters(parameterWithName("department-id").description("부서 ID")),
                            requestFields(
                                    fieldWithPath("name").type(STRING).description("부서 이름"),
                                    fieldWithPath("description").type(STRING).description("부서 설명").optional())
                            ));

            verify(departmentService).updateDepartment(request, 1L);
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void invalidInput() throws Exception {
            DepartmentUpdateRequest request = new DepartmentUpdateRequest("", "");

            mockMvc.perform(put("/api/core/departments/{department-id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            DepartmentUpdateRequest request = new DepartmentUpdateRequest("정형외과", "기획 부서");

            given(departmentService.updateDepartment(request, 1L)).willThrow(new ForbiddenException());

            mockMvc.perform(put("/api/core/departments/{department-id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 부서 없음")
        void notFound() throws Exception {
            DepartmentUpdateRequest request = new DepartmentUpdateRequest("정형외과", "기획 부서");

            given(departmentService.updateDepartment(request, 1L)).willThrow(new DepartmentNotFoundException());

            mockMvc.perform(put("/api/core/departments/{department-id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 부서 이름 중복")
        void duplicate() throws Exception {
            DepartmentUpdateRequest request = new DepartmentUpdateRequest("정형외과", "기획 부서");

            given(departmentService.updateDepartment(request, 1L)).willThrow(new DepartmentAlreadyExistsException());

            mockMvc.perform(put("/api/core/departments/{department-id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("부서 삭제 DELETE /api/core/departments/{department-id}")
    class deleteDepartment {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            mockMvc.perform(delete("/api/core/departments/{department-id}", 1L))
                    .andExpect(status().isNoContent())
                    .andDo(document("department-delete",
                            pathParameters(parameterWithName("department-id").description("부서 ID"))));

            verify(departmentService).deleteDepartment(1L);
        }

        @Test
        @DisplayName("실패 - 부서 없음")
        void notFound() throws Exception {
            willThrow(new DepartmentNotFoundException()).given(departmentService).deleteDepartment(1L);

            mockMvc.perform(delete("/api/core/departments/{department-id}", 1L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(departmentService).deleteDepartment(1L);

            mockMvc.perform(delete("/api/core/departments/{department-id}", 1L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    private List<FieldDescriptor> departmentInfoResponseFields() {
        return List.of(
                fieldWithPath("data.id").type(NUMBER).description("부서 ID"),
                fieldWithPath("data.name").type(STRING).description("부서 이름"),
                fieldWithPath("data.description").type(STRING).description("부서 설명").optional(),
                fieldWithPath("data.status").type(STRING).description("부서 상태"),
                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성 일시")
        );
    }
}
