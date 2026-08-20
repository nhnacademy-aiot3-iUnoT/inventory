package com.nhnacademy.inventory.inventories.alert.controller;

import com.nhnacademy.inventory.global.exception.ForbiddenException;
import com.nhnacademy.inventory.inventories.alert.domain.AlertType;
import com.nhnacademy.inventory.inventories.alert.dto.*;
import com.nhnacademy.inventory.inventories.alert.exception.AlertNotFoundException;
import com.nhnacademy.inventory.inventories.alert.service.AlertService;
import com.nhnacademy.inventory.support.RestDocsUtils;
import com.nhnacademy.inventory.support.SupportControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertController.class)
class AlertControllerTest extends SupportControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlertService alertService;

    @Nested
    @DisplayName("알림 조건 조회 GET /api/core/organizations/{organization-id}/alerts")
    class getAlerts{

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            AlertSearchCondition condition = new AlertSearchCondition(AlertType.LOW_STOCK, false);
            AlertInfoResponse response = new AlertInfoResponse(
                    1L, 11L, "테스트 조직",
                    AlertType.LOW_STOCK, "테스트 메시지",
                    false, LocalDateTime.now()
            );

            given(alertService.getAlerts(eq(11L), any(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.addAll(alertInfoResponseFields("data.content[]."));
            responseFields.addAll(pageResponseFields("data."));

            mockMvc.perform(get("/api/core/organizations/{organization-id}/alerts", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(condition)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].alertType").value("LOW_STOCK"))
                    .andExpect(jsonPath("$.data.content[0].message").value("테스트 메시지"))
                    .andDo(document("alert-get-list",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID")
                            ),
                            requestFields(
                                    fieldWithPath("alertType").type(JsonFieldType.STRING).description("메시지 유형"),
                                    fieldWithPath("isChecked").type(JsonFieldType.BOOLEAN).description("확인 유무")
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("정상 처리 테스트(빈 배열)")
        void success_empty() throws Exception {
            AlertSearchCondition condition = new AlertSearchCondition(AlertType.LOW_STOCK, false);

            given(alertService.getAlerts(eq(11L), any(), any(Pageable.class)))
                    .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

            mockMvc.perform(get("/api/core/organizations/{organization-id}/alerts", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(condition)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            AlertSearchCondition condition = new AlertSearchCondition(AlertType.LOW_STOCK, false);

            given(alertService.getAlerts(eq(11L), any(), any(Pageable.class)))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(get("/api/core/organizations/{organization-id}/alerts", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(condition)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("체크하지않은 알림 개수 조회 GET /api/core/organizations/{organization-id}/alerts/unread-count")
    class getUnreadAlertCount{

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            given(alertService.getUncheckedAlertCount(11L))
                    .willReturn(5L);

            List<FieldDescriptor> responseFields = new ArrayList<>(RestDocsUtils.successResponseFields());
            responseFields.add(fieldWithPath("data").type(JsonFieldType.NUMBER).description("체크되지 않은 알림 개수"));

            mockMvc.perform(get("/api/core/organizations/{organization-id}/alerts/unread-count", 11L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(5))
                    .andDo(document("alert-get-unchecked-count",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID")
                            ),
                            responseFields(
                                    responseFields
                            )
                    ));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            given(alertService.getUncheckedAlertCount(11L))
                    .willThrow(new ForbiddenException());

            mockMvc.perform(get("/api/core/organizations/{organization-id}/alerts/unread-count", 11L))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("알림 체크 PUT /api/core/organizations/{organization-id}/alerts/check")
    class markAsChecked{

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            AlertCheckRequest request = new AlertCheckRequest(
                    List.of(1L, 2L, 3L, 4L, 5L)
            );

            mockMvc.perform(put("/api/core/organizations/{organization-id}/alerts/check", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent())
                    .andDo(document("alert-check",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID")
                            ),
                            requestFields(
                                    fieldWithPath("alertIds").type(JsonFieldType.ARRAY).description("체크할 알림 ID 리스트")
                            )
                    ));

            verify(alertService).markAsChecked(11L, request);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 알림")
        void fail_NotFoundAlert() throws Exception{
            AlertCheckRequest request = new AlertCheckRequest(
                    List.of(1L, 2L, 3L, 4L, 5L)
            );

            willThrow(new AlertNotFoundException()).given(alertService)
                            .markAsChecked(11L, request);

            mockMvc.perform(put("/api/core/organizations/{organization-id}/alerts/check", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            AlertCheckRequest request = new AlertCheckRequest(
                    List.of(1L, 2L, 3L, 4L, 5L)
            );

            willThrow(new ForbiddenException()).given(alertService)
                    .markAsChecked(11L, request);

            mockMvc.perform(put("/api/core/organizations/{organization-id}/alerts/check", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("알림 삭제 DELETE /api/core/organizations/{organization-id}/alerts")
    class deleteAlerts{

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            AlertDeleteRequest request = new AlertDeleteRequest(
                    List.of(1L, 2L, 3L, 4L, 5L)
            );

            mockMvc.perform(delete("/api/core/organizations/{organization-id}/alerts", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent())
                    .andDo(document("alert-delete",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID")
                            ),
                            requestFields(
                                    fieldWithPath("alertIds").type(JsonFieldType.ARRAY).description("체크할 알림 ID 리스트")
                            )
                    ));

            verify(alertService).deleteAlerts(11L, request);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 알림")
        void fail_NotFoundAlert() throws Exception{
            AlertDeleteRequest request = new AlertDeleteRequest(
                    List.of(1L, 2L, 3L, 4L, 5L)
            );

            willThrow(new AlertNotFoundException()).given(alertService)
                    .deleteAlerts(11L, request);

            mockMvc.perform(delete("/api/core/organizations/{organization-id}/alerts", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            AlertDeleteRequest request = new AlertDeleteRequest(
                    List.of(1L, 2L, 3L, 4L, 5L)
            );

            willThrow(new ForbiddenException()).given(alertService)
                    .deleteAlerts(11L, request);

            mockMvc.perform(delete("/api/core/organizations/{organization-id}/alerts", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("알림 전체 삭제 DELETE /api/core/organizations/{organization-id}/alerts/all")
    class deleteAllAlerts{

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            mockMvc.perform(delete("/api/core/organizations/{organization-id}/alerts/all", 11L))
                    .andExpect(status().isNoContent())
                    .andDo(document("alert-delete-all",
                            pathParameters(
                                    parameterWithName("organization-id").description("조직 ID")
                            )
                    ));

            verify(alertService).deleteAllAlerts(11L);
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void fail_Forbidden() throws Exception {
            willThrow(new ForbiddenException()).given(alertService)
                    .deleteAllAlerts(11L);

            mockMvc.perform(delete("/api/core/organizations/{organization-id}/alerts/all", 11L))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("알림 생성 내부 api POST /api/core/internal/alerts")
    class createAlert{

        @Test
        @DisplayName("정상 처리 테스트")
        void success() throws Exception {
            AlertCreateRequest request = new AlertCreateRequest(11L, AlertType.LOW_STOCK, "테스트 메시지");

            mockMvc.perform(post("/api/core/internal/alerts", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent())
                    .andDo(document("alert-delete",
                            requestFields(
                                    fieldWithPath("organizationId").type(JsonFieldType.NUMBER).description("조직 ID"),
                                    fieldWithPath("alertType").type(JsonFieldType.STRING).description("알림 유형"),
                                    fieldWithPath("message").type(JsonFieldType.STRING).description("메시지")
                            )
                    ));

            verify(alertService).createAlert(request.organizationId(), request.alertType(), request.message());
        }

        @Test
        @DisplayName("실패 - 잘못된 입력")
        void fail_InvalidInput() throws Exception {
            AlertCreateRequest request = new AlertCreateRequest(null, AlertType.LOW_STOCK, "테스트 메시지");

            mockMvc.perform(post("/api/core/internal/alerts", 11L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(alertService, never()).createAlert(anyLong(), any(), anyString());
        }
    }

    private List<FieldDescriptor> alertInfoResponseFields(String prefix){
        return List.of(
                fieldWithPath(prefix + "alertId").type(JsonFieldType.NUMBER).description("알림 ID"),
                fieldWithPath(prefix + "organizationId").type(JsonFieldType.NUMBER).description("조직 ID"),
                fieldWithPath(prefix + "organizationName").type(JsonFieldType.STRING).description("조직 이름"),
                fieldWithPath(prefix + "alertType").type(JsonFieldType.STRING).description("알림 타입"),
                fieldWithPath(prefix + "message").type(JsonFieldType.STRING).description("메시지"),
                fieldWithPath(prefix + "isChecked").type(JsonFieldType.BOOLEAN).description("읽음 여부"),
                fieldWithPath(prefix + "createdAt").type(JsonFieldType.STRING).description("생성 시간")
        );
    }

    private List<FieldDescriptor> pageResponseFields(String prefix) {
        return List.of(
                fieldWithPath(prefix + "page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
                fieldWithPath(prefix + "size").type(JsonFieldType.NUMBER).description("페이지 크기"),
                fieldWithPath(prefix + "totalElements").type(JsonFieldType.NUMBER).description("전체 데이터 개수"),
                fieldWithPath(prefix + "totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 개수"),
                fieldWithPath(prefix + "last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부")
        );
    }
}