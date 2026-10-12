# Observed Urja portal protocol

This document is based on user-supplied browser captures, the served export-button JavaScript, and point-in-time live checks through the local API on 13 October 2026. Cookie values, credentials, and signing secrets are excluded.

## Authentication

The captured browser posts form-encoded `email` and `password` to `/login`, with `Accept: application/json` and `x-sveltekit-action: true`. HTTP 200 contains `{"type":"redirect","status":303,"location":"/meters"}`. That action result is not a wire-level HTTP 303. Portal reads carry a cookie named `__Secure-better-auth.session_token`. Cookie issuance, lifetime, failed login, and session-expiry behavior remain unverified.

## Data access

| GET route | Captured response |
| --- | --- |
| `/portal/meters/search?q=&page=1` | `data`, `total`, `page`, `pageSize`; 20 summaries, total 403 |
| `/portal/meters/J100000/geo` | `data` object with string latitude/longitude |
| `/portal/meters/J100000/energy` | `data` array; timestamp, string kwh/kvah/voltR; 337 readings |
| `/portal/dts?page=1` and `page=2` | Transformer code/name/feederCode/capacityKva; 40 total, 20 per page |
| `/portal/keys` | `data.signingSecret`; key for the verified export HMAC |
| `/portal/export?page=1` | 403 unique meters and total 403; installation fields, build, hierarchy, numeric geo |

`/meters`, `/transformers`, and `/meters/{id}` return HTML. The captured detail HTML contains nameplate parameter/value pairs and hierarchy in SvelteKit page data. A dedicated JSON nameplate route was not captured.

An unmatched search returns `{"data":[],"total":0,"page":1,"pageSize":20}`. Search semantics beyond the UI's meter-number/serial label and out-of-range pagination have not been verified.

## Quirks and limitations

- Form login uses a JSON action redirect result, so HTTP 200 alone is insufficient.
- Export includes all 403 captured records despite `page=1`; that does not prove the parameter's meaning.
- Export includes 238 legacy and 165 v2 records; one v2 meter (J100004) passed detail, location, and readings checks; exhaustive per-build coverage is unverified.
- Energy timestamps use `dd/MM/yyyy HH:mm`, without a timezone. Values appear cumulative but semantics need confirmation.
- Numeric strings and numeric JSON values must be normalized without manufacturing zero values.
- Hierarchy codes have conflicting names and parent paths. DT-007 has differing labels between the transformer list and some exported meter paths. Preserve source paths and investigate before building a canonical tree.
- Key lifetime, sign-out, rate limiting, expiry, and source freshness remain open investigation items. A nonexistent meter geo/energy request returned upstream 404; the current adapter maps it to 502 and needs better error classification.

See [ARCHITECTURE_ASSUMPTIONS.md](./ARCHITECTURE_ASSUMPTIONS.md) for reasoning and verification steps. Only normal read-only portal operations are in scope.

## Export signing verified on 13 October 2026

The portal's served transformer-page module `/_app/immutable/nodes/4.2Bgc2kUI.js` confirms its export flow: fetch `/portal/keys`, take `data.signingSecret`, and compute HMAC-SHA256 using its UTF-8 bytes over `GET`, `/portal/export`, `page=1`, and the current Unix timestamp in seconds, joined with newline characters and no trailing newline. Send lowercase hexadecimal signature in `x-signature` and the timestamp in `x-timestamp` alongside the session cookie.

The implementation now follows this verified flow with a fresh key and timestamp per export. Earlier references in this document to unverified signing describe the prior investigation state. Key values, session values, and captured signatures are not committed. Session expiry and legacy/v2 differences still need separate verification.

Hierarchy labels/codes may be blank or absent in source data. The mapper preserves blanks and uses null for absent values rather than rejecting the full export or inventing hierarchy identifiers.
