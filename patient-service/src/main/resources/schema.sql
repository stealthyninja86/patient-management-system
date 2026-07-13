CREATE SEQUENCE IF NOT EXISTS patient_id_seq START 100 INCREMENT 1;
CREATE SEQUENCE IF NOT EXISTS consent_seq START 100 INCREMENT 1;

CREATE TABLE IF NOT EXISTS outbox (
                                      id              UUID PRIMARY KEY,
                                      aggregate_type  VARCHAR(100)    NOT NULL,
    aggregate_id    VARCHAR(50)     NOT NULL,
    event_type      VARCHAR(100)    NOT NULL,
    payload         JSONB           NOT NULL,
    topic           VARCHAR(100)    NOT NULL,
    partition_key   VARCHAR(100),
    published       BOOLEAN         NOT NULL DEFAULT false,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished
    ON outbox (created_at)
    WHERE published = false;