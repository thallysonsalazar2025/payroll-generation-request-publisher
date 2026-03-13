# Responsibilities of the `payroll-generation-request-publisher` component

## Primary Objective
Receive a payslip generation request from the gateway and publish an event/command to a RabbitMQ topic/queue so the generator component can process the request.

## Recommended Responsibilities

### 1) Request contract validation
In addition to validating the object (`month`, `year`), the component should:
- Ensure `month` is between 1 and 12.
- Ensure `year` is within an acceptable range (e.g., >= 2000 and <= current year + 1).
- Reject invalid payloads with a clear error (HTTP 400).
- Validate format/requiredness of needed identifiers (e.g., `employeeId`, `companyId`, `requesterId`).

### 2) Minimal business-rule validation (pre-publication)
- Verify the requested period is allowed by policy (e.g., no invalid future date).
- Ensure the endpoint handles **one month/year per request**.
- Optional: block concurrent reprocessing of the same `employeeId + month + year` when a job is in progress.

### 3) Idempotency
Key responsibility to avoid duplicate messages:
- Accept/generate an `idempotencyKey`.
- Persist key control for a time window.
- On a repeated request, respond consistently without republishing the message.

### 4) Reliable publication to RabbitMQ
- Declare exchange/routing key/queue according to the contract.
- Publish with `message persistence` and minimal headers.
- Confirm publication (publisher confirms) before returning success to the client.
- On publication failure, return an appropriate error (e.g., HTTP 503/500) and log the cause.

### 5) Message enrichment and standardization
Produce a message with useful metadata for traceability:
- `requestId` / `correlationId`.
- `occurredAt` (timestamp).
- `source` (origin service).
- `schemaVersion`.
- Audit context (`requestedBy`, tenant, etc.).

### 6) Observability
- Structured logs with end-to-end correlation.
- Metrics: request volume, invalid validation rate, successful/error publication rate, latency.
- Distributed tracing (when applicable).

### 7) Security and compliance
- Validate identity/authorization at the expected domain level (even with a gateway in front, if needed).
- Sanitize sensitive data in logs.
- Apply least-privilege to RabbitMQ credentials.

### 8) Operational resilience
- Timeout and controlled retry for publication (with backoff).
- Circuit breaker (if it makes sense for the stack).
- Strategy for broker unavailability (graceful degradation + clear response).

### 9) Client response contract
Instead of waiting for synchronous generation, reply with the async pattern:
- `202 Accepted` with `requestId` and initial status (`RECEIVED`/`QUEUED`).
- Complementary status inquiry endpoint (if it exists in the ecosystem).

### 10) Event contract governance
- Version the message schema.
- Guarantee backward compatibility between publisher and consumer.
- Document the contract (required/optional fields and semantics).

## What is **not** this component's responsibility
- Generating the payslip file.
- Detailed payroll calculation.
- Heavy rules of the final processing (those belong to the consumer/generator).

## Minimum checklist (robust MVP)
- [ ] Validation of `month/year` + required identifiers.
- [ ] Rule of 1 period per request.
- [ ] Idempotency by business key.
- [ ] Publication with confirmation to RabbitMQ.
- [ ] `correlationId` + structured logs.
- [ ] `202 Accepted` response with `requestId`.
- [ ] Basic metrics and consistent error handling.

## Practical suggestion
If your current scope already covers validation and publication, the **next priorities** should be:
1. Idempotency.
2. Observability (correlationId + metrics).
3. Asynchronous response contract (`202 + requestId`).

These three points typically prevent rework and incidents in production as volume grows.
