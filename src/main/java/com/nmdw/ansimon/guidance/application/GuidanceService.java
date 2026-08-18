package com.nmdw.ansimon.guidance.application;

import com.nmdw.ansimon.elderly.domain.ElderlyProfile;
import com.nmdw.ansimon.elderly.infra.ElderlyProfileRepository;
import com.nmdw.ansimon.global.error.BusinessException;
import com.nmdw.ansimon.global.error.ErrorCode;
import com.nmdw.ansimon.guidance.domain.GuidanceStatus;
import com.nmdw.ansimon.guidance.domain.InterventionPlan;
import com.nmdw.ansimon.guidance.dto.GuidanceRequest;
import com.nmdw.ansimon.guidance.dto.InterventionPlanResponse;
import com.nmdw.ansimon.guidance.dto.InterventionPlanSummaryResponse;
import com.nmdw.ansimon.guidance.infra.InterventionPlanRepository;
import com.nmdw.ansimon.guidance.infra.client.GuidanceServiceClient;
import com.nmdw.ansimon.risk.domain.RiskSnapshot;
import com.nmdw.ansimon.risk.infra.RiskSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * 노인 요약·위치·최신 위험도를 조립해 별도 guidance 서버에 안내계획 생성을 요청하고 결과를 저장하는 애플리케이션 서비스입니다.
 * MVP 범위에서는 동의 상태를 확인하지 않으며, 실제 전화 발신(contact) 단계에서 동의 체크를 수행합니다.
 */
@Service
@Transactional
public class GuidanceService {

    private final ElderlyProfileRepository elderlyProfileRepository;
    private final RiskSnapshotRepository riskSnapshotRepository;
    private final GuidanceServiceClient guidanceServiceClient;
    private final InterventionPlanRepository interventionPlanRepository;
    private final ObjectMapper objectMapper;

    public GuidanceService(ElderlyProfileRepository elderlyProfileRepository,
                           RiskSnapshotRepository riskSnapshotRepository,
                           GuidanceServiceClient guidanceServiceClient,
                           InterventionPlanRepository interventionPlanRepository,
                           ObjectMapper objectMapper) {
        this.elderlyProfileRepository = elderlyProfileRepository;
        this.riskSnapshotRepository = riskSnapshotRepository;
        this.guidanceServiceClient = guidanceServiceClient;
        this.interventionPlanRepository = interventionPlanRepository;
        this.objectMapper = objectMapper;
    }

    public InterventionPlanSummaryResponse generatePlan(Long elderlyId) {
        ElderlyProfile elderly = elderlyProfileRepository.findById(elderlyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
        RiskSnapshot riskSnapshot = riskSnapshotRepository
                .findTopByRegionCodeOrderByGeneratedAtDesc(elderly.getRegionCode())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        GuidanceRequest request = new GuidanceRequest(
                new GuidanceRequest.ElderlySummary(elderly.getDisplayName(), elderly.getPhone(),
                        elderly.getAge(), elderly.getHealthNote()),
                new GuidanceRequest.LocationSummary(elderly.getLatitude(), elderly.getLongitude()),
                new GuidanceRequest.RiskSummary(riskSnapshot.getRiskScore(), riskSnapshot.getRiskLevel(),
                        riskSnapshot.getGeneratedAt())
        );

        InterventionPlanResponse response = guidanceServiceClient.requestPlan(request);

        InterventionPlan plan = InterventionPlan.builder()
                .elderly(elderly)
                .riskSnapshot(riskSnapshot)
                .status(GuidanceStatus.READY_FOR_REVIEW)
                .guidanceJson(writeJson(Map.of(
                        "actionGuidance", response.actionGuidance(),
                        "shelterRecommendationText", response.shelterRecommendationText())))
                .questionsJson(writeJson(response.callQuestionOrder()))
                .evidenceChunkIdsJson(writeJson(response.evidenceDocumentIds()))
                .build();

        InterventionPlan saved = interventionPlanRepository.save(plan);
        return InterventionPlanSummaryResponse.from(saved);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, exception);
        }
    }
}
