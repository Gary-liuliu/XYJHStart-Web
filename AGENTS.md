# AGENTS.md

## Code Commenting Rules

Core principle: reject redundant "code translation" comments. Comments should preserve business intent, implicit constraints, and non-obvious risks.

### Zero Comments By Default
- Do not add comments for intuitive CRUD, UI rendering, or utility logic.
- Prefer self-explanatory names over explanatory comments.
- Avoid comments that merely restate what the code does.

### Comment Only Why Or Constraints
Add short comments only when one of these applies:
- Counter-intuitive design: unusual code exists for performance, compatibility, or a known bug workaround.
- Business red line: logic or hardcoded values have business meaning and must not be changed casually.
- Implicit dependency: code depends on external environment, global state, API shape, or runtime data format.

### Contract-Style Comments
For core functions, classes, or components over roughly 50 lines, use one concise header comment only when it clarifies prerequisites, output, or side effects.

Recommended format:

```java
// [Function] Handles order status transitions. [Warning] Validate inventory first to prevent overselling.
```

### Naming Requirement
- Use semantic names such as `isPendingPayment` and `calculateTax`.
- Avoid ambiguous names such as `flag`, `status`, `calc`, or `do` when domain intent is knowable.

## Project Notes

- Backend: Spring Boot on port `9191`.
- Frontend: Vue 3 + Element Plus under `XYJHStart-front`.
- Account trade page entry: `XYJHStart-front/src/views/Home.vue`.
- Account API: `/api/account-xyjh/**`.
- Refresh desktop sync API: `POST /api/account-xyjh/sync/refresh`.
- Sync header: `X-XYJH-Sync-Token`.
- Sync secret property: `xyjh.sync.secret`, default `EgoKaKa`; environment `XYJH_SYNC_SECRET` overrides it.

## Sync Contract

- Refresh only syncs accounts whose local type is `trade`.
- The sync endpoint upserts by raw `account` first, then by `accountName`.
- Desktop sync must never require an admin JWT, because the refresh tool runs outside the admin browser session.
- If changing the sync payload, update both `SyncAccountXYJHRequest` and `CloudAccountSyncClient`.
## Frontend Mobile Account Page

- Mobile account trade page is a delivery/copy workflow; keep search, account table, and pagination visible inside the mobile viewport.
- Mobile account rows must preserve status-based row colors, with the status column using a deeper shade than the row background.
- Mobile pagination must remain usable so delivery staff can access all account pages, not only the currently visible rows.
