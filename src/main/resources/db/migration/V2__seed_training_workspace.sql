-- Synthetic fixtures for the private training workspace; no customer data.
INSERT INTO operators (display_name, active) VALUES
('Alex Morgan', TRUE), ('Sam Rivera', TRUE), ('Taylor Chen', TRUE), ('Retired Queue', FALSE);

INSERT INTO tickets (title, description, status, priority, assignee_id, created_at, updated_at) VALUES
('Invoice export delayed', 'Finance batch export does not finish before the daily reconciliation window.', 'IN_PROGRESS', 'HIGH', 1, '2026-01-12T08:00:00Z', '2026-01-12T10:15:00Z'),
('Warehouse label alignment', 'Labels from the packing station are shifted on the second page.', 'NEW', 'LOW', 1, '2026-01-12T08:30:00Z', '2026-01-12T08:30:00Z'),
('Supplier portal unavailable', 'The procurement team reports intermittent portal timeouts.', 'WAITING', 'URGENT', 2, '2026-01-13T09:00:00Z', '2026-01-13T11:00:00Z'),
('Duplicate delivery notification', 'Dispatch receives two confirmations for one delivery.', 'NEW', 'NORMAL', NULL, '2026-01-13T09:30:00Z', '2026-01-13T09:30:00Z'),
('Inventory reconciliation mismatch', 'A regional warehouse reports a difference after the nightly stock import.', 'RESOLVED', 'HIGH', 3, '2026-01-14T07:00:00Z', '2026-01-15T12:00:00Z'),
('Monthly report header', 'The operations report should show the reporting period in the exported header.', 'NEW', 'LOW', NULL, '2026-01-14T10:00:00Z', '2026-01-14T10:00:00Z'),
('Dispatch queue sorting', 'Dispatch screen now follows the agreed departure ordering.', 'CLOSED', 'NORMAL', 2, '2026-01-10T08:00:00Z', '2026-01-16T14:00:00Z'),
('Invoice attachment preview', 'Reviewing scanned invoice attachments occasionally leaves the preview empty.', 'NEW', 'HIGH', 1, '2026-01-16T09:00:00Z', '2026-01-16T09:00:00Z');

INSERT INTO ticket_history(ticket_id, event_type, detail, created_at)
SELECT id, 'CREATED', 'Ticket registered', created_at FROM tickets;
INSERT INTO ticket_history(ticket_id, event_type, detail, created_at)
SELECT id, 'ASSIGNED', 'Assigned to operator ' || assignee_id, created_at + INTERVAL '1 minute'
FROM tickets WHERE assignee_id IS NOT NULL;
INSERT INTO ticket_history(ticket_id, event_type, detail, created_at)
SELECT id, 'STATUS_CHANGED', 'NEW -> IN_PROGRESS', created_at + INTERVAL '2 minutes'
FROM tickets WHERE status <> 'NEW';
INSERT INTO ticket_history(ticket_id, event_type, detail, created_at) VALUES
(3, 'STATUS_CHANGED', 'IN_PROGRESS -> WAITING', '2026-01-13T11:00:00Z'),
(5, 'STATUS_CHANGED', 'IN_PROGRESS -> RESOLVED', '2026-01-15T12:00:00Z'),
(7, 'STATUS_CHANGED', 'IN_PROGRESS -> RESOLVED', '2026-01-16T13:00:00Z'),
(7, 'STATUS_CHANGED', 'RESOLVED -> CLOSED', '2026-01-16T14:00:00Z');
INSERT INTO ticket_comments(ticket_id, author_id, body, created_at) VALUES
(1, 1, 'Export reaches the reconciliation stage. Collecting a smaller batch for comparison.', '2026-01-12T10:15:00Z'),
(3, 2, 'Waiting for a fresh availability report from procurement.', '2026-01-13T11:00:00Z'),
(5, 3, 'Reconciliation completed after reviewing the source import.', '2026-01-15T12:00:00Z');
INSERT INTO ticket_history(ticket_id, event_type, detail, created_at)
SELECT ticket_id, 'COMMENT_ADDED', 'Comment added by operator ' || author_id, created_at FROM ticket_comments;
