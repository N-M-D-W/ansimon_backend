package com.nmdw.ansimon.guidance.api;

import com.nmdw.ansimon.global.error.GlobalExceptionHandler;
import com.nmdw.ansimon.guidance.application.GuidanceService;
import com.nmdw.ansimon.guidance.domain.GuidanceStatus;
import com.nmdw.ansimon.guidance.dto.InterventionPlanSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GuidanceController.class)
@Import(GlobalExceptionHandler.class)
class GuidanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GuidanceService guidanceService;

    @Test
    void triggersPlanGenerationForAnElderlyId() throws Exception {
        when(guidanceService.generatePlan(eq(1L))).thenReturn(new InterventionPlanSummaryResponse(
                10L, 1L, GuidanceStatus.READY_FOR_REVIEW, "{\"actionGuidance\":\"...\"}",
                "[\"호흡 확인\"]", "[\"kma-guide-2024-03\"]", Instant.now()));

        mockMvc.perform(post("/internal/v1/guidance/plans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("READY_FOR_REVIEW"));
    }
}
