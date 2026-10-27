# API contract

Base URL: `http://127.0.0.1:8080`. Requests and successful responses use JSON. Identifiers are positive integers. Operator identities are synthetic references, not authenticated users.

| Method | Path | Purpose | Success |
|---|---|---|---|
| GET | `/api/operators` | List operators, including inactive entries | 200 |
| GET | `/api/tickets` | Filter and page ticket summaries | 200 |
| POST | `/api/tickets` | Register an incident | 201 + Location |
| GET | `/api/tickets/{id}` | Detail, comments, audit history and allowed transitions | 200 |
| PATCH | `/api/tickets/{id}` | Replace editable title, description and priority | 200 |
| PUT | `/api/tickets/{id}/assignee` | Assign an active operator | 200 |
| POST | `/api/tickets/{id}/transitions` | Request a new state | 200 |
| POST | `/api/tickets/{id}/comments` | Add a comment from an active operator | 201 |

Search parameters: `status`, `priority`, `assigneeId`, `q` (case-insensitive title text), `page` (zero-based, default 0), `size` (1–100, default 20). Supplied filters are intended to be combined. Response envelope: `content`, `page`, `size`, `totalElements`, `totalPages`.

Priorities: `LOW`, `NORMAL`, `HIGH`, `URGENT`. States: `NEW`, `IN_PROGRESS`, `WAITING`, `RESOLVED`, `CLOSED`. Titles have a 140-character limit, descriptions 4000, comments 2000. PATCH requires all three editable fields. Ticket deletion is outside this API; closed tickets retain history and are read-only.

Registration:
```http
POST /api/tickets
Content-Type: application/json

{"title":"Warehouse export delayed","description":"The daily export has not completed.","priority":"HIGH"}
```

Assignment, transition and comment bodies:
```json
{"operatorId":1}
```
```json
{"status":"IN_PROGRESS"}
```
```json
{"authorId":1,"body":"Checking the latest batch."}
```

Read the returned `Location`, then GET that ticket to inspect persisted status, comments and history. NEW tickets require assignment before work can start. Work can wait and resume; resolved incidents can reopen or close. Use the detail response's `allowedTransitions` to guide the next operation.
