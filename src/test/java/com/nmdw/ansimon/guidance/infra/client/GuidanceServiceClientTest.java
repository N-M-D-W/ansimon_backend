package com.nmdw.ansimon.guidance.infra.client;

import com.nmdw.ansimon.global.error.ErrorCode;
import com.nmdw.ansimon.global.error.ExternalServiceException;
import com.nmdw.ansimon.guidance.dto.GuidanceRequest;
import com.nmdw.ansimon.risk.domain.RiskLevel;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuidanceServiceClientTest {

    @Test
    void requestsAPlanAndReturnsTheParsedResponse() {
        WebClient webClient = WebClient.builder().exchangeFunction(request -> {
            assertThat(request.url().getPath()).isEqualTo("/v1/plans");
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("""
                    {"actionGuidance":"실내에 머무르며 수분을 섭취하세요.",
                     "shelterRecommendationText":"종로구민센터 쉼터로 이동을 권장합니다.",
                     "callQuestionOrder":["호흡 확인","실내 온도 확인"],
                     "evidenceDocumentIds":["kma-guide-2024-03"]}
                    """).build());
        }).build();
        GuidanceServiceClient client = new GuidanceServiceClient(webClient);

        var response = client.requestPlan(sampleRequest());

        assertThat(response.actionGuidance()).isEqualTo("실내에 머무르며 수분을 섭취하세요.");
        assertThat(response.callQuestionOrder()).containsExactly("호흡 확인", "실내 온도 확인");
    }

    @Test
    void rejectsAResponseMissingRequiredFieldsAsAnExternalServiceFailure() {
        WebClient webClient = WebClient.builder().exchangeFunction(request -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body("{\"actionGuidance\":\"\",\"shelterRecommendationText\":\"\",\"callQuestionOrder\":[]}")
                        .build()
        )).build();
        GuidanceServiceClient client = new GuidanceServiceClient(webClient);

        assertThatThrownBy(() -> client.requestPlan(sampleRequest()))
                .isInstanceOfSatisfying(ExternalServiceException.class,
                        exception -> assertThat(exception.errorCode())
                                .isEqualTo(ErrorCode.EXTERNAL_SERVICE_SERVER_ERROR));
    }

    private GuidanceRequest sampleRequest() {
        return new GuidanceRequest(
                new GuidanceRequest.ElderlySummary(75, "고혈압"),
                new GuidanceRequest.LocationSummary(new BigDecimal("37.5730000"), new BigDecimal("126.9794000")),
                new GuidanceRequest.RiskSummary(new BigDecimal("0.8000"), RiskLevel.HIGH, Instant.now())
        );
    }
}
