CREATE TABLE options
(
    name            VARCHAR(255) NOT NULL,
    value           TEXT,
    updated_at_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_options PRIMARY KEY (name)
);
