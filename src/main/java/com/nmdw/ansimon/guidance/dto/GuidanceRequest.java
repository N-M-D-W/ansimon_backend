package com.nmdw.ansimon.guidance.dto;

import com.nmdw.ansimon.risk.domain.RiskLevel;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 노인 요약, 위치, 최신 위험도 스냅샷을 별도 LLM/RAG guidance 서버로 전달하는 요청입니다.
 * 전화번호·주소 원문·동의 상태는 포함하지 않아 전달되는 개인정보를 최소화합니다.
 */
public record GuidanceRequest(
        ElderlySummary elderly,
        LocationSummary location,
        RiskSummary risk
) {
    public record ElderlySummary(Integer age, String healthNote) {
    }

    public record LocationSummary(BigDecimal latitude, BigDecimal longitude) {
    }

    public record RiskSummary(BigDecimal score, RiskLevel level, Instant generatedAt) {
    }
}
