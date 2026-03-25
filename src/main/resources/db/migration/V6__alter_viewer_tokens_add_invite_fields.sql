ALTER TABLE viewer_tokens
    ADD COLUMN code              VARCHAR(20)  NOT NULL DEFAULT '',
    ADD COLUMN delivery_status   VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    ADD COLUMN sent_at           TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN guest_name        VARCHAR(255),
    ADD COLUMN guest_contact     VARCHAR(255);

CREATE UNIQUE INDEX idx_viewer_tokens_code ON viewer_tokens (code);
UPDATE viewer_tokens SET code = UPPER(SUBSTRING(MD5(RANDOM()::TEXT), 1, 3)) || '-' ||
    LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0') WHERE code = '';
ALTER TABLE viewer_tokens ALTER COLUMN code DROP DEFAULT;
