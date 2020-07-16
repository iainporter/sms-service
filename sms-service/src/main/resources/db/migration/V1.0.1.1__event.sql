CREATE TABLE outboxevent
(
    id uuid PRIMARY KEY,
    aggregatetype VARCHAR(75),
    aggregateid VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    payload varchar(4096) NOT NULL
)
;

CREATE TABLE processedevent
(
    eventid uuid PRIMARY KEY,
    processedat TIMESTAMP NOT NULL
)
;



