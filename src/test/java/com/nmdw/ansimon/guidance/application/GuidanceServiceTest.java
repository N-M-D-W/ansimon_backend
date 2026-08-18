package com.nmdw.ansimon.guidance.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nmdw.ansimon.elderly.domain.ConsentStatus;
import com.nmdw.ansimon.elderly.domain.ElderlyProfile;
import com.nmdw.ansimon.elderly.infra.ElderlyProfileRepository;
import com.nmdw.ansimon.global.error.BusinessException;
import com.nmdw.ansimon.guidance.domain.GuidanceStatus;
import com.nmdw.ansimon.guidance.domain.InterventionPlan;
import com.nmdw.ansimon.guidance.dto.InterventionPlanResponse;
import com.nmdw.ansimon.guidance.dto.InterventionPlanSummaryResponse;
import com.nmdw.ansimon.guidance.infra.InterventionPlanRepository;
import com.nmdw.ansimon.guidance.infra.client.GuidanceServiceClient;
import com.nmdw.ansimon.risk.domain.RiskLevel;
import com.nmdw.ansimon.risk.domain.RiskSnapshot;
import com.nmdw.ansimon.risk.infra.RiskSnapshotRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GuidanceServiceTest {

    private final ElderlyProfileRepository elderlyProfileRepository = mock(ElderlyProfileRepository.class);
    private final RiskSnapshotRepository riskSnapshotRepository = mock(RiskSnapshotRepository.class);
    private final GuidanceServiceClient guidanceServiceClient = mock(GuidanceServiceClient.class);
    private final InterventionPlanRepository interventionPlanRepository = mock(InterventionPlanRepository.class);
    private final GuidanceService service = new GuidanceService(elderlyProfileRepository, riskSnapshotRepository,
            guidanceServiceClient, interventionPlanRepository, new ObjectMapper());

    @Test
    void assemblesTheRequestAndPersistsTheReturnedPlan() {
        when(elderlyProfileRepository.findById(1L)).thenReturn(Optional.of(elderly()));
        when(riskSnapshotRepository.findTopByRegionCodeOrderByGeneratedAtDesc("11110"))
                .thenReturn(Optional.of(riskSnapshot()));
        when(guidanceServiceClient.requestPlan(any())).thenReturn(new InterventionPlanResponse(
                "실내에 머무르며 수분을 섭취하세요.", "종로구민센터 쉼터로 이동을 권장합니다.",
                List.of("호흡 확인", "실내 온도 확인"), List.of("kma-guide-2024-03")));
        when(interventionPlanRepository.save(any(InterventionPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        InterventionPlanSummaryResponse plan = service.generatePlan(1L);

        assertThat(plan.status()).isEqualTo(GuidanceStatus.READY_FOR_REVIEW);
        assertThat(plan.guidanceJson()).contains("실내에 머무르며 수분을 섭취하세요.");
        assertThat(plan.questionsJson()).contains("호흡 확인");
    }

    @Test
    void rejectsGenerationWhenNoRiskSnapshotExistsForTheRegion() {
        when(elderlyProfileRepository.findById(1L)).thenReturn(Optional.of(elderly()));
        when(riskSnapshotRepository.findTopByRegionCodeOrderByGeneratedAtDesc("11110")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generatePlan(1L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectsGenerationWhenTheElderlyDoesNotExist() {
        when(elderlyProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generatePlan(99L)).isInstanceOf(BusinessException.class);
    }

    private ElderlyProfile elderly() {
        return ElderlyProfile.builder().displayName("김안심").phone("010").address("서울시 종로구 1-1")
                .latitude(new BigDecimal("37.5730000")).longitude(new BigDecimal("126.9794000"))
                .regionCode("11110").consentStatus(ConsentStatus.CONSENTED).build();
    }

    private RiskSnapshot riskSnapshot() {
        return RiskSnapshot.builder()
                .regionCode("11110").riskScore(new BigDecimal("0.8000")).riskLevel(RiskLevel.HIGH)
                .targetStartAt(Instant.now()).targetEndAt(Instant.now().plusSeconds(3600))
                .modelVersion("test-model-v1").generatedAt(Instant.now()).build();
    }
}
