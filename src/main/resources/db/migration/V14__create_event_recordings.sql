CREATE TABLE event_recordings (
    id          UUID        NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id    UUID        NOT NULL REFERENCES events(id),
    s3_key      TEXT        NOT NULL,
    quality     TEXT        NOT NULL,
    duration_ms BIGINT      NOT NULL DEFAULT 0,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_event_recordings_event_id ON event_recordings(event_id);
