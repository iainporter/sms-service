CREATE SCHEMA if NOT EXISTS sms;

CREATE TABLE sms.message
(
    id uuid PRIMARY KEY,
    from_number VARCHAR(32),
    to_number VARCHAR(32) NOT NULL,
    text VARCHAR(1600) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
)
;



