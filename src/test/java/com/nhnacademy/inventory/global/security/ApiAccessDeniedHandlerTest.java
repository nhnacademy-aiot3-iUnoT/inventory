package com.nhnacademy.inventory.global.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiAccessDeniedHandler accessDeniedHandler = new ApiAccessDeniedHandler(
            new SecurityErrorResponseWriter(objectMapper)
    );

    @Test
    void writesForbiddenErrorResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/core/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(request, response, new AccessDeniedException("access denied"));

        JsonNode body = objectMapper.readTree(response.getContentAsByteArray());

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        assertTrue(MediaType.APPLICATION_JSON.isCompatibleWith(
                MediaType.parseMediaType(response.getContentType())
        ));
        assertFalse(body.get("success").asBoolean());
        assertEquals("G002", body.get("error").get("code").asText());
        assertEquals("권한이 없습니다.", body.get("error").get("message").asText());
    }
}
