package com.xmh.checkin;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebApiIntegrationTest {
  @Autowired MockMvc mvc;

  @Test
  void loginReturnsTokenAndTraceId() throws Exception {
    mvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-Id", "api-trace-1")
                .content("{\"authCode\":\"dev-alice\"}"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Request-Id", "api-trace-1"))
        .andExpect(jsonPath("$.code").value("OK"))
        .andExpect(jsonPath("$.traceId").value("api-trace-1"))
        .andExpect(jsonPath("$.data.token").isNotEmpty());
  }

  @Test
  void protectedApiUsesUnifiedUnauthorizedResponse() throws Exception {
    mvc.perform(get("/api/v1/meetings").header("X-Request-Id", "api-trace-2"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("AUTH_REQUIRED"))
        .andExpect(jsonPath("$.traceId").value("api-trace-2"));
  }

  @Test
  void invalidPayloadUsesUnifiedErrorResponse() throws Exception {
    mvc.perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Request-Id", "api-trace-3")
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
        .andExpect(jsonPath("$.traceId").value("api-trace-3"));
  }
}
