package com.nmdw.ansimon.guidance.dto;

import java.util.List;

/**
 * guidance 서버가 생성한 안내계획 응답입니다.
 * 근거 문서 ID는 저장 후 원천 문서 레지스트리와 대조할 수 있도록 그대로 보관합니다.
 */
public record InterventionPlanResponse(
        String actionGuidance,
        String shelterRecommendationText,
        List<String> callQuestionOrder,
        List<String> evidenceDocumentIds
) {
}
