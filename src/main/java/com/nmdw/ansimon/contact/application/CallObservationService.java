package com.nmdw.ansimon.contact.application;

import com.nmdw.ansimon.contact.domain.CallObservation;
import com.nmdw.ansimon.contact.dto.CallObservationResponse;
import com.nmdw.ansimon.contact.dto.CallObservationUpdateRequest;
import com.nmdw.ansimon.contact.infra.CallObservationRepository;
import com.nmdw.ansimon.global.error.BusinessException;
import com.nmdw.ansimon.global.error.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 저장된 통화 결과를 담당자가 교정하거나 삭제하는 유스케이스를 담당하는 애플리케이션 서비스입니다.
 * 후속 지원 업무 판단이 이 판정값에 의존하므로, LLM이 잘못 판단한 항목을 사람이 바로잡을 수 있게 합니다.
 */
@Service
@Transactional
public class CallObservationService {

    private final CallObservationRepository repository;

    public CallObservationService(CallObservationRepository repository) {
        this.repository = repository;
    }

    public CallObservationResponse update(Long id, CallObservationUpdateRequest request) {
        CallObservation observation = findById(id);
        observation.correct(
                StringUtils.hasText(request.summary()) ? request.summary() : observation.getSummary(),
                request.shelterIntent() != null ? request.shelterIntent() : observation.getShelterIntent(),
                request.canMoveAlone() != null ? request.canMoveAlone() : observation.getCanMoveAlone(),
                request.helpNeeded() != null ? request.helpNeeded() : observation.getHelpNeeded(),
                request.symptomMentioned() != null ? request.symptomMentioned() : observation.getSymptomMentioned());
        return CallObservationResponse.from(observation);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private CallObservation findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
