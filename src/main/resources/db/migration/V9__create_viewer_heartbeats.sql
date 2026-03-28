CREATE TABLE viewer_heartbeats (
    session_id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    last_ping TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_viewer_heartbeats_event_last_ping ON viewer_heartbeats(event_id, last_ping);
