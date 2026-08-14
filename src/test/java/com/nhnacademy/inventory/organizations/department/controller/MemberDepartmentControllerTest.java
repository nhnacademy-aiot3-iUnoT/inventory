package com.nhnacademy.inventory.organizations.department.controller;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.organizations.department.exception.DepartmentNotFoundException;
import com.nhnacademy.inventory.organizations.department.exception.MemberDepartmentAlreadyExistsException;
import com.nhnacademy.inventory.organizations.department.exception.MemberDepartmentNotInException;
import com.nhnacademy.inventory.organizations.department.service.MemberDepartmentService;
import com.nhnacademy.inventory.organizations.member.exception.OrgMemberNotFoundException;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberDepartmentController.class)
class MemberDepartmentControllerTest extends SupportControllerTest {
    @MockitoBean
    private MemberDepartmentService memberDepartmentService;

    @Nested
    @DisplayName("조직원 부서 할당 POST /api/core/members/{member-id}/departments/{department-id}")
    class assign_department {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            mockMvc.perform(post("/api/core/members/{member-id}/departments/{department-id}", 1L, 1L))
                            .andExpect(status().isNoContent())
                            .andDo(document("assign-department",
                                    pathParameters(
                                            parameterWithName("member-id").description("조직원 ID"),
                                            parameterWithName("department-id").description("부서 ID"))
                            ));

            verify(memberDepartmentService).assignDepartment(1L, 1L);
        }

        @Test
        @DisplayName("실패 - 이미 지정되어 있음")
        void conflict() throws Exception {
            willThrow(new MemberDepartmentAlreadyExistsException()).given(memberDepartmentService).assignDepartment(1L, 1L);

            mockMvc.perform(post("/api/core/members/{member-id}/departments/{department-id}", 1L, 1L))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("실패 - 조직원이 존재하지 않음")
        void notFound_member() throws Exception {
            willThrow(new OrgMemberNotFoundException()).given(memberDepartmentService).assignDepartment(1L, 1L);

            mockMvc.perform(post("/api/core/members/{member-id}/departments/{department-id}", 1L, 1L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 부서가 존재하지 않음")
        void notFound_department() throws Exception {
            willThrow(new DepartmentNotFoundException()).given(memberDepartmentService).assignDepartment(1L, 1L);

            mockMvc.perform(post("/api/core/members/{member-id}/departments/{department-id}", 1L, 1L))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(memberDepartmentService).assignDepartment(1L, 1L);

            mockMvc.perform(post("/api/core/members/{member-id}/departments/{department-id}", 1L, 1L))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("조직원 부서 해제 POST /api/core/members/{member-id}/departments/{department-id}")
    class remove_department {
        @Test
        @DisplayName("성공")
        void success() throws Exception {
            mockMvc.perform(delete("/api/core/members/{member-id}/departments/{department-id}", 1L, 1L))
                    .andExpect(status().isNoContent())
                    .andDo(document("assign-department",
                            pathParameters(
                                    parameterWithName("member-id").description("조직원 ID"),
                                    parameterWithName("department-id").description("부서 ID"))
                    ));

            verify(memberDepartmentService).removeDepartment(1L, 1L);
        }

        @Test
        @DisplayName("실패 - 지정되어 있지 않음")
        void badRequest() throws Exception {
            willThrow(new MemberDepartmentNotInException()).given(memberDepartmentService).removeDepartment(1L, 1L);

            mockMvc.perform(delete("/api/core/members/{member-id}/departments/{department-id}", 1L, 1L))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(memberDepartmentService).removeDepartment(1L, 1L);

            mockMvc.perform(delete("/api/core/members/{member-id}/departments/{department-id}", 1L, 1L))
                    .andExpect(status().isForbidden());
        }
    }
}
