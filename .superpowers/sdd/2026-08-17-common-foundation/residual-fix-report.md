# Residual Fix Report

## Delivered

- Added a safe 406 API envelope for `HttpMediaTypeNotAcceptableException`.
- Added a safe `ResponseStatusException` handler that preserves all Spring 4xx statuses without exposing exception reasons or raw details; non-4xx response-status exceptions collapse to the generic internal-error envelope.
- Routed WebClient error-response body release through the sanitized response wrapper.
- Added sanitized `releaseBody()` mapping for wrapped successful responses.
- Extended generic `body(BodyExtractor)` handling to sanitize both reactive-stream failures and failures thrown synchronously while invoking the extractor.

## Verification Constraint

Per explicit user instruction, no tests and no build commands were written or run for this residual fix wave. Inspection was limited to source diff review and `git diff --check`.
