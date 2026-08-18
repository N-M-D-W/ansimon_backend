package com.nmdw.ansimon.guidance.api;

import com.nmdw.ansimon.global.response.ApiResponse;
import com.nmdw.ansimon.guidance.application.GuidanceService;
import com.nmdw.ansimon.guidance.dto.InterventionPlanSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 특정 노인에 대해 별도 guidance 서버로 안내계획 생성을 수동으로 트리거하는 내부 API입니다.
 * 위험도 임계값 기반 자동 트리거가 준비되기 전까지 개발·QA 목적의 진입점으로 사용합니다.
 */
@Tag(name = "안내계획 (내부)", description = "노인 정보와 최신 위험도로 별도 guidance 서버에 안내계획 생성을 요청합니다.")
@RestController
@RequestMapping("/internal/v1/guidance")
public class GuidanceController {

    private final GuidanceService guidanceService;

    public GuidanceController(GuidanceService guidanceService) {
        this.guidanceService = guidanceService;
    }

    @Operation(summary = "안내계획 수동 생성", description = "지정한 노인의 최신 위험도 스냅샷으로 guidance 서버에 안내계획 생성을 요청하고 결과를 저장합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "노인 또는 위험도 스냅샷을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "guidance 서버 오류"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "504", description = "guidance 서버 시간 초과")
    })
    @PostMapping("/plans/{elderlyId}")
    public ApiResponse<InterventionPlanSummaryResponse> generatePlan(
            @Parameter(description = "노인 프로필 식별자", example = "1", required = true)
            @PathVariable Long elderlyId) {
        return ApiResponse.success(guidanceService.generatePlan(elderlyId));
    }
}
