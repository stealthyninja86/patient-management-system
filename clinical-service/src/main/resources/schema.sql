CREATE SEQUENCE IF NOT EXISTS prescription_id_seq START WITH 100;
CREATE SEQUENCE IF NOT EXISTS drug_id_seq START WITH 100;

CREATE TABLE IF NOT EXISTS outbox (
                                      id              UUID PRIMARY KEY,
                                      aggregate_type  VARCHAR(100)    NOT NULL,
    aggregate_id    VARCHAR(50)     NOT NULL,
    event_type      VARCHAR(100)    NOT NULL,
    payload         TEXT           NOT NULL,
    topic           VARCHAR(100)    NOT NULL,
    partition_key   VARCHAR(100),
    published       BOOLEAN         NOT NULL DEFAULT false,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_outbox_unpublished
    ON outbox (created_at)
    WHERE published = false;