CREATE TABLE viewer_sessions (
    id              UUID                        NOT NULL PRIMARY KEY,
    viewer_token_id UUID                        NOT NULL REFERENCES viewer_tokens(id),
    event_id        UUID                        NOT NULL REFERENCES events(id),
    jti             VARCHAR(255)                NOT NULL UNIQUE,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    issued_at       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    expires_at      TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    revoked         BOOLEAN                     NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_viewer_sessions_jti      ON viewer_sessions (jti);
CREATE INDEX idx_viewer_sessions_event_id ON viewer_sessions (event_id);
CREATE INDEX idx_viewer_sessions_token_id ON viewer_sessions (viewer_token_id);
