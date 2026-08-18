package com.nmdw.ansimon.risk.application;

import com.nmdw.ansimon.global.error.BusinessException;
import com.nmdw.ansimon.global.error.ErrorCode;
import com.nmdw.ansimon.global.util.RegionCodes;
import com.nmdw.ansimon.risk.domain.RiskLevel;
import com.nmdw.ansimon.risk.domain.RiskSnapshot;
import com.nmdw.ansimon.risk.dto.MlForecastResponse;
import com.nmdw.ansimon.risk.dto.RiskForecastRequest;
import com.nmdw.ansimon.risk.dto.RiskForecastResponse;
import com.nmdw.ansimon.risk.infra.RiskSnapshotRepository;
import com.nmdw.ansimon.risk.infra.client.MlClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * ML 예측 API를 호출해 위험도 스냅샷을 만들고 저장하는 애플리케이션 서비스입니다.
 * riskScore는 의료 진단이 아니라 예방 연락 우선순위 지표이며, 등급 산정은 이 서비스가
 * ML 점수에 설정 기반 정책(RiskPolicyProperties)을 결합해 계산합니다.
 */
@Service
@Transactional
public class RiskService {

    private static final ZoneId ASIA_SEOUL = ZoneId.of("Asia/Seoul");
    private static final String DEFAULT_REGION = "서울";

    private final MlClient mlClient;
    private final RiskSnapshotRepository riskSnapshotRepository;
    private final RiskPolicyProperties riskPolicyProperties;
    private final ObjectMapper objectMapper;

    public RiskService(MlClient mlClient, RiskSnapshotRepository riskSnapshotRepository,
                       RiskPolicyProperties riskPolicyProperties, ObjectMapper objectMapper) {
        this.mlClient = mlClient;
        this.riskSnapshotRepository = riskSnapshotRepository;
        this.riskPolicyProperties = riskPolicyProperties;
        this.objectMapper = objectMapper;
    }

    public RiskForecastResponse forecast(RiskForecastRequest request) {
        String region = StringUtils.hasText(request.region()) ? request.region() : DEFAULT_REGION;
        MlForecastResponse mlResponse = mlClient.forecast(region);

        BigDecimal score = BigDecimal.valueOf(mlResponse.elderlyHeatIllnessRiskScore())
                .setScale(4, RoundingMode.HALF_UP);
        RiskLevel level = resolveLevel(score);
        LocalDate forecastDate = LocalDate.parse(mlResponse.date());
        String regionCode = RegionCodes.siDoCodeForName(mlResponse.region());

        RiskSnapshot snapshot = RiskSnapshot.builder()
                .regionCode(regionCode)
                .riskScore(score)
                .riskLevel(level)
                .targetStartAt(atSeoulHour(forecastDate, riskPolicyProperties.getTargetStartHour()))
                .targetEndAt(atSeoulHour(forecastDate, riskPolicyProperties.getTargetEndHour()))
                .peakStartAt(atSeoulHour(forecastDate, riskPolicyProperties.getPeakStartHour()))
                .peakEndAt(atSeoulHour(forecastDate, riskPolicyProperties.getPeakEndHour()))
                .modelVersion(mlResponse.modelVersion())
                .topFactorsJson(writeTopFactors(mlResponse))
                .generatedAt(Instant.now())
                .build();

        RiskSnapshot saved = riskSnapshotRepository.save(snapshot);
        return RiskForecastResponse.from(saved);
    }

    private RiskLevel resolveLevel(BigDecimal score) {
        if (score.compareTo(riskPolicyProperties.getCriticalThreshold()) >= 0) {
            return RiskLevel.CRITICAL;
        }
        if (score.compareTo(riskPolicyProperties.getHighThreshold()) >= 0) {
            return RiskLevel.HIGH;
        }
        if (score.compareTo(riskPolicyProperties.getMediumThreshold()) >= 0) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }

    private Instant atSeoulHour(LocalDate date, int hour) {
        return date.atTime(hour, 0).atZone(ASIA_SEOUL).toInstant();
    }

    private String writeTopFactors(MlForecastResponse response) {
        return writeJson(List.of(
                "heatwaveRiskScore=" + response.heatwaveRiskScore(),
                "heatwavePrediction=" + response.heatwavePrediction(),
                "illnessWeatherEstimation=" + response.illnessWeatherEstimation(),
                "historicalAnalogRows=" + response.historicalAnalogRows()));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, exception);
        }
    }
}
