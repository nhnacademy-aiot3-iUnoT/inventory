package com.nhnacademy.inventory.support;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import java.util.List;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class RestDocsUtils {

    // 성공 응답 공통 필드
    public static List<FieldDescriptor> successResponseFields() {
        return List.of(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (true)"),
                fieldWithPath("error").type(JsonFieldType.OBJECT).optional().description("에러 정보 (성공 시 null)"),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간")
        );
    }

    // 실패(에러) 응답 공통 필드
    public static List<FieldDescriptor> errorResponseFields() {
        return List.of(
                fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
                fieldWithPath("data").type(JsonFieldType.OBJECT).optional().description("데이터 (실패 시 null)"),
                fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
                fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
                fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시간")
        );
    }
}
