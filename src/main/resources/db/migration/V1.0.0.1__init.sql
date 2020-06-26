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
CREATE UNIQUE INDEX sms_to_number_idx on sms.message(to_number);
CREATE UNIQUE INDEX sms_status_idx on sms.message(status);
CREATE UNIQUE INDEX sms_created_at_idx on sms.message(created_at);



