# Postman reviewer guide

1. Install Java 21 and run the Spring Boot service following the project README. Set portal credentials in the server environment, not in Postman.
2. Import `Urjameter-API.postman_collection.json` into Postman. This single file contains all required defaults.
3. No environment selection or manual request inputs are required. The collection targets `http://127.0.0.1:8080` and resolves its variables from collection defaults, ignoring environment overrides.
4. Run the collection in order. Request 01 fills `meterId`, `search`, and `serialNumber` from a real list result. The default values are examples from supplied captures. Captured values are saved automatically in the collection. The separate environment file is optional and is not needed by this collection.
5. Inspect test results and response bodies. All requests are read-only GETs and require no body or consumer authentication.

The collection includes 17 requests covering all six public routes, search/clear search, pagination, expected empty results, input errors, and the served OpenAPI specification. Back navigation and switching UI sections are frontend behaviors and need no endpoint.

Signed detail/export access has passed point-in-time live checks. Long-running session expiry remains unverified. Success tests expect 200 and fail if the service returns an upstream error. The collection was checked for JSON structure, route coverage and script syntax; its 17 request cases have been checked through HTTP against the live-backed local application, but the scripts have not been executed in Postman/Newman.

Expected input-validation responses are 400 problem JSON. Missing server credentials produce 503; portal failures produce 502; timeouts produce 504. Missing-meter semantics and session-expiry recovery remain unverified, so this collection does not invent a 404 or a public login/logout route. Portal signing keys are not exposed by this API.

Include this folder in the recruiter-facing repository. Do not include portal cookies, passwords or signing secrets in exported environments, saved responses or screenshots.
