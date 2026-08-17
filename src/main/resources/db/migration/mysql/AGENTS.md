# 안심온 데이터베이스 구조

## 개요

이 문서는 `V1__create_initial_schema.sql`에 정의된 안심온 MVP의 MySQL 구조를 설명한다. 시스템은 고령자의 폭염 위험을 예측하고, 대응계획·예방전화·통화 결과·후속 지원 업무까지 연결한다.

```text
elderly_profile
  ├─ intervention_plan ─ contact_job ─ call_observation
  │                              └─ support_task
  └─ support_task

risk_snapshot ─ intervention_plan
shelter ─ intervention_plan (선택)
```

## 테이블

### elderly_profile - 고령자 프로필

관리 대상 고령자의 기본 정보와 자동 연락 동의 상태를 저장한다.

- 주요 컬럼: `display_name`, `phone`, `address`, `latitude`, `longitude`, `region_code`, `consent_status`
- 인덱스: `region_code`, `phone`
- 제약: 위도는 -90~90, 경도는 -180~180 범위
- 활용: 지역별 위험 대상 조회, 전화 동의 확인, 대응계획 및 지원 업무의 기준 대상

### risk_snapshot - 지역 위험도

지역과 시간 구간별 폭염 위험 예측 결과를 저장한다.

- 주요 컬럼: `region_code`, `risk_score`, `risk_level`, `target_start_at`, `target_end_at`, `peak_start_at`, `peak_end_at`, `top_factors_json`, `model_version`
- 인덱스: `region_code + target_start_at + target_end_at`
- 제약: 위험 점수는 0~1, 대상 시간은 시작 시각보다 종료 시각이 늦어야 함
- 활용: 고위험 지역 대상자의 대응계획 생성과 연락 우선순위 판단

### shelter - 무더위쉼터

공공데이터 등 외부 원천에서 수집한 쉼터 정보와 운영 상태를 저장한다.

- 주요 컬럼: `source_id`, `name`, `address`, `latitude`, `longitude`, `open_status`, `source_version`
- 유일값: `source_id`
- 제약: 위도와 경도 범위
- 활용: 개인별 안내계획에서 추천할 쉼터 선택

### intervention_plan - 대응계획

고령자와 위험도 결과를 결합해 생성한 개인별 안내계획이다.

- 외래키: `elderly_id → elderly_profile`, `risk_snapshot_id → risk_snapshot`, `shelter_id → shelter`(선택)
- 주요 컬럼: `status`, `guidance_json`, `questions_json`, `evidence_chunk_ids_json`
- 활용: 안내문·질문·RAG 근거를 보관하고 전화 작업의 기준 계획으로 사용

### contact_job - 예방 전화 작업

승인된 대응계획에 따라 예약·실행·재시도하는 전화 작업이다.

- 외래키: `elderly_id → elderly_profile`, `intervention_plan_id → intervention_plan`
- 주요 컬럼: `status`, `attempt_count`, `scheduled_at`, `approved_by`, `approved_at`, `last_attempt_at`, `next_retry_at`, `provider_call_id`, `failure_reason`, `lock_token`, `locked_until`
- 중복 방지: `idempotency_key`는 유일함
- 전화사업자 식별: `provider_call_id`는 유일함
- 활용: 전화 승인, 예약 실행, 미응답 재시도, 외부 전화 API 연동

### call_observation - 통화 결과

전화 종료 후 구조화된 응답과 분석 결과를 전화 작업당 하나씩 저장한다.

- 외래키: `contact_job_id → contact_job`
- 유일값: `contact_job_id`
- 주요 컬럼: `contact_status`, `shelter_intent`, `can_move_alone`, `help_needed`, `symptom_mentioned`, `summary`, `transcript_ref`, `confidence`, `ended_at`
- 제약: 신뢰도는 0~1
- 활용: 도움 필요, 이동 곤란, 증상 언급을 판단해 지원 업무를 생성

### support_task - 후속 지원 업무

통화 결과 또는 운영자 판단으로 생성되는 실제 지원 업무다.

- 외래키: `elderly_id → elderly_profile`, `contact_job_id → contact_job`(선택)
- 주요 컬럼: `task_type`, `priority`, `status`, `reason`, `due_at`, `assignee_id`, `assigned_at`, `completed_at`, `completion_note`
- 중복 방지: `deduplication_key`는 필수·유일값
- 활용: 쉼터 이동 지원, 안부 확인, 수동 전화 확인 등 후속 조치 관리

## 데이터 처리 흐름

1. 고령자 프로필을 등록한다.
2. 지역·시간대 위험도를 `risk_snapshot`에 저장한다.
3. 고령자와 위험도를 조합해 `intervention_plan`을 생성한다.
4. 승인된 계획으로 `contact_job`을 예약·실행한다.
5. 통화 결과를 `call_observation`에 저장한다.
6. 지원이 필요하면 `support_task`를 생성·배정·완료 처리한다.

## 상태 관리 시 유의사항

- 자동 전화는 `consent_status`가 동의 상태인 대상자에게만 생성한다.
- 동일 전화 작업은 `idempotency_key`로 중복 생성하지 않는다.
- 스케줄러는 `locked_until`이 만료된 작업만 가져가고, 처리 전 잠금을 획득해야 한다.
- 통화 결과와 지원 업무는 중복 웹훅에도 중복 저장되지 않도록 유일 제약을 사용한다.
- `transcript_ref`에는 원문 대신 접근 제어된 녹취·전사 참조값을 저장한다.
