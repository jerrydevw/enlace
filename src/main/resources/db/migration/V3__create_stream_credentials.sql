CREATE TABLE stream_credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL UNIQUE REFERENCES events(id),
    ivs_stream_key_arn VARCHAR(255) NOT NULL,
    rtmp_endpoint VARCHAR(255) NOT NULL,
    stream_key VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
