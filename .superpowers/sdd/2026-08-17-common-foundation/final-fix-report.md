# Final Fix Report

## Delivered

- Configured Hibernate JDBC time handling as UTC in both local and production runtime profiles. Per user direction, no H2 persistence round-trip test was added.
- Extended the global MVC exception envelope to preserve safe 4xx responses for request binding/validation failures, missing parameters, unsupported media types, and unsupported methods.
- Added stable public error codes for HTTP 415 and 405 and pinned every `ErrorCode.code()` string in tests.
- Wrapped successful WebClient responses so decoder and response-body stream failures map to sanitized external-service exceptions without retaining raw causes.
- Made the sanitized external-service exception a WebClient exception so `retrieve()` does not re-wrap it with request details, while preserving explicit global API error handling.
- Reworked the Jackson test to use the Spring Boot-managed `ObjectMapper`, proving the customizer is applied by Boot.

## TDD Evidence

- RED: focused test compilation failed because the new 415/405 public error codes did not exist.
- RED: after the first WebClient wrapper implementation, decoder and body-stream tests exposed Spring `retrieve()` re-wrapping the sanitized exception in `WebClientResponseException`.
- GREEN: focused MVC, ErrorCode, Jackson, WebClient mapper, decoder, and body-stream tests passed.

## Verification

- `./gradlew.bat test` — BUILD SUCCESSFUL
- `./gradlew.bat build` — BUILD SUCCESSFUL
- `git diff --check` — clean (only Git's existing line-ending notice for `ErrorCodeTest.java`)
