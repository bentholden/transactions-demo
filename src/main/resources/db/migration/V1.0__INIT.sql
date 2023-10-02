create table T_RECORD
(
    ID     UUID not null primary key,
    RECORD TEXT not null
);

create table T_KAFKA_OUTBOX
(
    ID              UUID not null primary key,
    TOPIC           TEXT NOT NULL,
    PAYLOAD         TEXT not null,
    CREATED_DATE    DATE not null,
    SENT_DATE       DATE
);