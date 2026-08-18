package com.nmdw.ansimon.guidance.infra.client;

import com.nmdw.ansimon.global.error.ErrorCode;
import com.nmdw.ansimon.global.error.ExternalServiceException;
import com.nmdw.ansimon.guidance.dto.GuidanceRequest;
import com.nmdw.ansimon.guidance.dto.InterventionPlanResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 별도 LLM/RAG guidance 서버에 노인 요약·위치·위험도를 전달하고 안내계획 응답을 받는 어댑터입니다.
 * 필수 필드가 비어 있는 응답은 guidance 서버 실패로 간주해 {@link ExternalServiceException}으로 변환합니다.
 */
@Component
public class GuidanceServiceClient {

    private final WebClient guidanceWebClient;

    public GuidanceServiceClient(@Qualifier("guidanceWebClient") WebClient guidanceWebClient) {
        this.guidanceWebClient = guidanceWebClient;
    }

    public InterventionPlanResponse requestPlan(GuidanceRequest request) {
        InterventionPlanResponse response = guidanceWebClient.post()
                .uri("/v1/plans")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(InterventionPlanResponse.class)
                .block();

        validate(response);
        return response;
    }

    private void validate(InterventionPlanResponse response) {
        if (response == null
                || !StringUtils.hasText(response.actionGuidance())
                || !StringUtils.hasText(response.shelterRecommendationText())
                || CollectionUtils.isEmpty(response.callQuestionOrder())) {
            throw new ExternalServiceException(ErrorCode.EXTERNAL_SERVICE_SERVER_ERROR);
        }
    }
}
