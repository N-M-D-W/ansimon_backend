package com.nmdw.ansimon.guidance.dto;

import com.nmdw.ansimon.risk.domain.RiskLevel;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GuidanceRequestTest {

    private final JsonMapper objectMapper = JsonMapper.builder().build();

    @Test
    void serializesRiskLevelAsItsLowercaseStableCode() throws Exception {
        GuidanceRequest request = new GuidanceRequest(
                new GuidanceRequest.ElderlySummary("김안심", "010-1234-5678", 75, "고혈압, 당뇨"),
                new GuidanceRequest.LocationSummary(new BigDecimal("37.5730000"), new BigDecimal("126.9794000")),
                new GuidanceRequest.RiskSummary(new BigDecimal("0.8000"), RiskLevel.HIGH,
                        Instant.parse("2026-08-18T01:00:00Z"))
        );

        String json = objectMapper.writeValueAsString(request);

        assertThat(json).contains("\"level\":\"high\"");
        assertThat(json).contains("\"age\":75");
        assertThat(json).contains("\"name\":\"김안심\"");
        assertThat(json).contains("\"phone\":\"010-1234-5678\"");
    }
}
