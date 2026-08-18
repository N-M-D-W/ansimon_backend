package com.nmdw.ansimon.guidance.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nmdw.ansimon.risk.domain.RiskLevel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GuidanceRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void serializesRiskLevelAsItsLowercaseStableCode() throws Exception {
        GuidanceRequest request = new GuidanceRequest(
                new GuidanceRequest.ElderlySummary(75, "고혈압, 당뇨"),
                new GuidanceRequest.LocationSummary(new BigDecimal("37.5730000"), new BigDecimal("126.9794000")),
                new GuidanceRequest.RiskSummary(new BigDecimal("0.8000"), RiskLevel.HIGH,
                        Instant.parse("2026-08-18T01:00:00Z"))
        );

        String json = objectMapper.writeValueAsString(request);

        assertThat(json).contains("\"level\":\"high\"");
        assertThat(json).contains("\"age\":75");
    }
}
