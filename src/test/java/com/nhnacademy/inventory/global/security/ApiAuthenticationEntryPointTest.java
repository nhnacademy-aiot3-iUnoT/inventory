package com.nhnacademy.inventory.global.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiAuthenticationEntryPoint entryPoint = new ApiAuthenticationEntryPoint(
            new SecurityErrorResponseWriter(objectMapper)
    );

    @Test
    void writesUnauthorizedErrorResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/core/inventories");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("invalid token"));

        JsonNode body = objectMapper.readTree(response.getContentAsByteArray());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(MediaType.APPLICATION_JSON.isCompatibleWith(
                MediaType.parseMediaType(response.getContentType())
        ));
        assertFalse(body.get("success").asBoolean());
        assertEquals("G004", body.get("error").get("code").asText());
        assertEquals("인증이 필요합니다.", body.get("error").get("message").asText());
    }
}
