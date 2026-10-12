# Assignment requirements audit

Date: 13 October 2026. Verdict: core API works in point-in-time checks; submission deliverables are not fully finished.

## Required deliverables

| Requirement | Finding |
| --- | --- |
| API source code | Present: six public read-only routes, models, services, portal client, login/cookie transport, export signer and errors |
| README: purpose, structure, setup and sample request | Present; stale signing/live-verification statements corrected during this audit |
| Assumptions, decisions, trade-offs, omissions, future improvements | Present in README and linked architecture documents; planning diagrams must be read as proposals |
| PROTOCOL.md | Present: captured routes, form login, cookie behavior, export signing and data quirks |
| OpenAPI 3.x for the new API | Present: OpenAPI 3.1.0, six paths; root and served copies agree and local schema references resolve |
| Five reflection answers | Incomplete: README contains questions only. Candidate must supply their actual experience |
| Public GitHub repository | Not verified; no Git remote configured locally and implementation files remain uncommitted |
| Email repository link | Not performed; requires explicit user instruction to send |

Swagger is optional and has been removed as requested. Postman is an extra reviewer convenience, not a replacement for OpenAPI. Database, frontend, cache, hierarchy reconstruction, and local index are optional; omitting them does not by itself fail the required scope.

## Verification performed

- Maven test: 13 tests, zero failures/errors.
- All 17 collection request cases returned expected HTTP statuses against the running local service: list/search/serial search/clear/empty search, meter pagination, detail, location, readings, transformer pagination, export, four input errors, and OpenAPI.
- Signed export returned 403 meters. Legacy J100000 and v2 J100004 detail, location and readings returned 200; this does not establish universal data coverage.
- Root and served OpenAPI files match; schema references resolve. No full external OpenAPI validator was run.
- Known password/session-value literal scan found no matches in source, Postman files or .env.example. This is a limited scan, not a complete Git-history secret audit.
- Swagger source pages and dependency are removed. Existing test results do not establish expired-session recovery or all failure behaviors.

## Remaining engineering findings

1. A nonexistent meter returns 502, including when upstream geo/energy returns 404. Introduce proper resource-not-found classification and update the contract/tests if implemented.
2. Every transport-level 502 on normal reads invalidates authentication, even if caused by networking or parsing. Classify authentication separately. Signed export bypasses that invalidation wrapper, so failures are handled inconsistently.
3. Detail lookup exports and maps the whole dataset for every call. This is documented, but an unrelated malformed meter can prevent a valid detail response. A separate detail strategy or validated cache is a future improvement.
4. TransformerService reuses MeterService's static pagination validation. This is unnecessary service coupling; shared validation would be clearer.
5. Earlier LLD interfaces and snapshot components are proposed abstractions, not implemented classes. The working application uses concrete constructor-injected clients and no snapshot.

## Ground rules and candidate preparation

Operations reviewed are normal reads and login; no paid service is required. The PDF says AI is for guidance and the core investigation must be the candidate's. The code and export investigation received substantial assistant help, so do not represent all implementation/investigation as independently performed. The candidate should understand and reproduce the login, signing, data normalization and trade-offs, and write their own reflection answers before submission.

## Submission sequence

1. Write the five personal reflection answers.
2. Resolve or explicitly retain and explain the engineering limitations above.
3. Review the README and protocol against the final implementation.
4. Review changed files and credentials, then commit and push to the intended public GitHub repository.
5. Send the repository link after confirming the recipient and authorization.
