# guidance

## Responsibility
Spring AI 기반 LLM/RAG로 공식 근거를 검색하고 개인별 행동지침, 추천 쉼터 안내문, 전화 질문 순서를 생성/검증합니다.

## Structure
```bash
guidance/
├── api/           # 안내 계획 생성/조회 API
├── application/   # 컨텍스트 조립, guidance 서버 호출, 결과 검증/저장
├── domain/        # InterventionPlan, GuidanceStatus, EvidenceChunkRef 등
├── infra/
│   └── client/    # 별도 LLM/RAG guidance 서버를 호출하는 WebClient 어댑터
└── dto/           # 안내 계획 요청/응답 DTO
```

## Rules
- 정부/공공기관 공식 문서만 운영 지침의 RAG 근거로 사용합니다.
- LLM 응답은 JSON/DTO로 구조화하고 enum, 필수 필드, 근거 ID를 검증합니다.
- 숫자, 주소, 운영시간, 전화번호는 LLM 결과를 신뢰하지 말고 원천 데이터와 대조합니다.
- 응급 증상 관련 문구는 자유 생성보다 승인된 고정 템플릿을 우선 사용합니다.
- 쉼터 후보 검색(TMAP 포함)은 이 저장소가 아니라 별도 LLM/RAG guidance 서버가 자체적으로 처리합니다. 이 패키지는 노인 요약·위치·위험도 스냅샷을 조립해 그 서버로 전달하고 응답을 검증/저장하는 역할만 합니다.
- 안내계획(`InterventionPlan`) 생성 시점에는 동의(`consent_status`) 여부를 확인하지 않습니다(MVP 범위). `elderly/AGENTS.md`의 동의 규칙은 `contact` 단계(실제 전화 발신)에 한정되며, 그 단계에서는 반드시 동의 체크를 거쳐야 합니다.
