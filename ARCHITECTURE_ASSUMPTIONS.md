# Architecture Assumptions and Design Reasoning

This document records uncertainty in the proposed Urjameter API architecture. It distinguishes observations from the supplied browser recording and request captures from inferences and implementation recommendations. It is a planning document, not a claim that the service has been implemented or these hypotheses tested.

The evidence was reviewed on 12 October 2026. No independent portal requests were made for this review. A successful captured request establishes behavior for that request only; it does not establish behavior for every meter, session, or dataset size.

## Evidence baseline

The supplied captures show:

- JSON meter search responses with `data`, `total`, `page`, and `pageSize`. One response contains 20 records and reports 403 total meters.
- Authenticated portal requests carrying a cookie named `__Secure-better-auth.session_token`.
- A form-encoded `POST /login` with `email` and `password`, `Accept: application/json`, and `x-sveltekit-action: true`. Its HTTP status is 200 and its JSON action result describes a redirect with status 303 to `/meters`.
- A `GET /portal/keys` response containing a field named `signingSecret`.
- A captured `GET /portal/export?page=1` response containing 403 meter records and reporting `total: 403`. Records include installation details, hierarchy, and coordinates.
- Meter-specific geo and energy JSON responses. The supplied energy response for J100000 contains 337 readings.
- Two transformer pages reporting 40 total transformers, with a page size of 20.
- HTML routes for the UI. The meter detail HTML contains SvelteKit page data with nameplate details and hierarchy.

Credentials, cookie values, and signing-secret values are deliberately excluded from this document. Redact them from public examples and test fixtures.

## 1. Cookie handling in the HTTP client

**Status:** Cookie transmission is observed. Automatic cookie storage is a proposed implementation choice. The mechanism by which login establishes the cookie remains unverified.

**Reasoning:** Browser data requests contain a session cookie, so the application needs a way to obtain and send the session credential. A cookie store associated with the portal HTTP client is preferable to copying a browser token into source code. Cookie handling must be explicitly configured for the chosen Java client; using `RestClient` or `WebClient` alone does not prove cookies will persist across requests.

**Verification:** Inspect the login response and any redirects for `Set-Cookie`, including domain, path, Secure, HttpOnly, and expiry attributes. From a fresh client, perform the captured login flow and then a normal authenticated read. Confirm the cookie is scoped to the portal and is not forwarded elsewhere.

**If disproved:** Implement the actual observed session-establishment flow. Do not invent a bearer-token scheme or hard-code the captured session value.

## 2. Login success and request headers

**Status:** The captured form request and successful action response are observed. Required headers, failed-login behavior, and alternative login modes are unverified.

**Reasoning:** HTTP 200 is insufficient as a login-success check because the captured response describes a SvelteKit action result. Start with the observed browser flow, inspect its result, and verify access through an authenticated read. Do not reproduce every browser header automatically.

**Verification:** Capture a failed login and a successful login from a fresh session. Establish whether `Origin`, `Referer`, or `x-sveltekit-action` is necessary and whether a preceding GET supplies any required state. Examine redirect behavior and cookie issuance. Use normal login interactions only.

**If disproved:** Update the login adapter and its success criteria to match the actual protocol. Keep credentials in configuration rather than accepting them on every public meter request.

## 3. Session expiry and renewal

**Status:** Expiry detection and renewal by repeating login are proposed behaviors, not observed portal behavior.

**Reasoning:** A long-running service should recover from a lost session. A bounded recovery attempt avoids infinite login loops. An expired session might produce a redirect, HTML login page, JSON error, or an HTTP error; the captures do not establish which.

**Verification:** Sign out normally or observe natural expiry, then record a read request's status, content type, redirect chain, and body. Determine whether repeating the normal login flow restores access.

**Proposed policy:** Renew only after a verified authentication-failure signal. Coordinate concurrent renewal attempts, then retry the read at most once. Do not treat every 403 or parsing failure as session expiry.

**If disproved:** Return a clear upstream authentication error and document any manual recovery requirement.

## 4. Export signing

**Status:** Unconfirmed inference.

**Reasoning:** A response field named `signingSecret` and a nearby export request suggest a signing mechanism. They do not prove that export requires signing, nor reveal the algorithm, signed text, headers, or key lifetime. The earlier diagram's signing component was a placeholder for investigation.

**Verification:** Inspect the successful export request's full headers and the browser initiator code available to the logged-in user. Identify whether the secret is used, the exact algorithm and message format, any timestamp or nonce, and when the key is refreshed. Reproduce the observed request only after the format is understood.

**If disproved:** Remove the signing component. If export cannot be reproduced reliably, use verified search and detail access instead. Keep any secret internal to the portal adapter and out of logs and public responses.

## 5. Export completeness and pagination

**Status:** One export response contains 403 records and matches its reported total. Global completeness, supported query parameters, and behavior at larger scale remain unverified.

**Reasoning:** This is promising bulk access and may avoid many per-meter requests. The presence of `page=1` does not establish pagination, and one captured response does not establish a permanent dataset limit.

**Verification:** Check unique meter IDs, count versus total, required fields, and coverage against the paginated search results. Inspect the export button's request construction. Record documented observations about parameters rather than guessing their meaning.

**If disproved:** Implement the observed export continuation mechanism or fall back to verified pagination. Do not call a partial export the full dataset.

## 6. In-memory meter snapshot and no database

**Status:** Proposed design choice, not portal behavior and not an assignment requirement.

**Reasoning:** The captured dataset is small enough to consider an in-memory map indexed by meter ID. This can support detail lookup, hierarchy, and optional filtering without persistence or migration overhead. A database becomes useful for durable history, a larger index, multiple service instances, or more advanced queries.

**Verification:** Measure export payload size, parse time, memory use, and refresh latency. Verify that exported records contain the fields needed by the public detail endpoint.

**Trade-offs:** State is lost on restart; each service instance has its own snapshot; details may lag behind live search. This approach should be documented as appropriate to the observed scale, not unlimited scale.

**If unsuitable:** Begin with live portal access, or add persistent storage when the chosen features require it. The architecture must not depend on export before export access is verified.

## 7. Snapshot freshness and refresh policy

**Status:** Configurable expiry, lazy loading, and coordinated refresh are recommendations. No portal freshness guarantee has been observed.

**Reasoning:** A cached snapshot reduces repeated bulk requests, but it needs an explicit age and failure policy. A fetch time indicates when the service obtained data, not when the utility updated it.

**Proposed policy:** Load on first use, refresh after a configurable interval, and publish the new snapshot atomically only after validation. Expose `fetchedAt` and document the configured expiry. If refresh fails, return an upstream error initially; stale fallback can be added only with an explicit maximum age and visible freshness metadata. No particular expiry duration has been justified yet.

**Verification:** Compare normal repeated exports over time, observe any upstream cache metadata, and check refresh behavior under concurrent requests and failures using local test doubles.

## 8. Data normalization and timestamp timezone

**Status:** Mixed numeric representations and timezone-free timestamp strings are observed. Choosing a timezone is unverified.

**Reasoning:** Geo and energy responses supply numeric strings, while export supplies numeric coordinates. Public DTOs should provide consistent types. `BigDecimal` is a proposed choice for energy values. Parse the observed timestamps with `dd/MM/yyyy HH:mm`; use a local date-time until the source timezone is known.

**Verification:** Inspect other meters for missing values, malformed values, extra phase readings, and alternate formats. Seek source evidence for timezone and document any eventual assumption explicitly.

**If uncertain:** Preserve the source timestamp and disclose that its timezone is unknown. Do not append `Z` or silently assign UTC. Do not convert invalid or absent readings into zero.

## 9. Cumulative energy readings

**Status:** Plausible inference, not confirmed semantics.

**Reasoning:** Consecutive sample kWh readings rise from 48438.74 to 48439.16, consistent with cumulative meter registers. This does not prove that all meters use cumulative values or establish reset and rollover behavior.

**Verification:** Examine readings from additional meters, labels or metadata, ordering, gaps, decreases, and resets. Confirm meaning before deriving usage.

**Proposed policy:** Expose the captured values as readings. If cumulative semantics are confirmed, interval usage may be derived by subtraction with explicit handling for gaps, resets, and invalid values. Do not sum cumulative registers as consumption.

## 10. Hierarchy identity and conflicting names

**Status:** A name conflict is observed. The authoritative naming source and globally unique hierarchy identifiers are unverified.

**Evidence:** The transformer listing names DT-007 `Sanganer DT 7`, while three exported meter hierarchies name DT-007 `Old Malviya Nagar Xfmr`.

**Reasoning:** Matching by code is preferable to matching by display name, but code uniqueness across parents must still be checked. Choosing one name as authoritative would be a design policy, not an established portal fact.

**Verification:** Check duplicate codes, different parent paths, missing hierarchy levels, and name conflicts throughout the export. Compare transformer feeder references with meter hierarchy feeder references.

**Proposed policy:** Retain the source names and flag discrepancies. Build hierarchy nodes using parent-qualified identities if codes are reused. Do not silently merge conflicting records or infer parentage from naming patterns.

## 11. Error mapping and retry policy

**Status:** Successful empty search is observed. Missing-meter, timeout, rate-limit, and malformed-response behavior are unverified. Public error handling is a design choice.

**Reasoning:** The clean API should distinguish an empty search from an unavailable upstream. A missing individual resource can map to 404 only when absence is established; a failed upstream read must not be converted into an empty result.

**Verification:** Observe a normal lookup for a nonexistent meter and an unauthenticated read. Simulate timeouts, malformed responses, and upstream failures locally rather than disrupting the portal.

**Proposed policy:** Empty search returns 200 with an empty list. Validate upstream status, content type, and response shape. Use bounded timeouts and consistent errors. Retry only eligible read failures with a bounded policy; do not retry indefinitely or expose portal secrets in errors.

## 12. Protecting the public API

**Status:** Deferring Spring Security is a scope recommendation, not a claim that the portal is unauthenticated.

**Reasoning:** Portal authentication belongs to the outbound client. Authentication of consumers of the new API is a separate requirement. A local take-home demonstration can initially avoid a second login system.

**Scope:** This recommendation assumes local execution. The service's portal credentials and session remain internal. Consumer authentication and authorization should be revisited before exposing the service beyond that intended scope.

## Verification order before committing to the design

1. Establish login success, cookie issuance, and authenticated read behavior.
2. Establish whether export signing is required and reproduce the verified export flow.
3. Validate export completeness and field coverage.
4. Observe expired-session and missing-meter behavior.
5. Check numeric formats, timestamp semantics, and hierarchy conflicts.
6. Choose and document caching, error, and naming policies based on those results.

## Relationship to submission documents

- `PROTOCOL.md` should describe verified portal behavior and clearly identify remaining uncertainty.
- `README.md` should describe the actual implemented architecture, setup, trade-offs, limitations, and link to this document.
- `openapi.json` should describe the implemented public API, not assumed portal routes.
- Reflection answers should describe the candidate's actual investigation and experience; this planning document is not a substitute for them.

Suggested README link:

```markdown
See [Architecture assumptions and reasoning](./ARCHITECTURE_ASSUMPTIONS.md)
for evidence, open questions, design choices, and verification steps.
```

## Export signing verified on 13 October 2026

The portal's served transformer-page module `/_app/immutable/nodes/4.2Bgc2kUI.js` confirms its export flow: fetch `/portal/keys`, take `data.signingSecret`, and compute HMAC-SHA256 using its UTF-8 bytes over `GET`, `/portal/export`, `page=1`, and the current Unix timestamp in seconds, joined with newline characters and no trailing newline. Send lowercase hexadecimal signature in `x-signature` and the timestamp in `x-timestamp` alongside the session cookie.

The implementation now follows this verified flow with a fresh key and timestamp per export. Earlier references in this document to unverified signing describe the prior investigation state. Key values, session values, and captured signatures are not committed. Session expiry and legacy/v2 differences still need separate verification.
