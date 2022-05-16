CREATE TABLE policy (
    id UUID NOT NULL,
    customerid VARCHAR NOT NULL,
    name VARCHAR NOT NULL,
    description VARCHAR,
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    conditions VARCHAR NOT NULL,
    mtime TIMESTAMP DEFAULT NOW(),
    actions VARCHAR,
    ctime TIMESTAMP DEFAULT NOW(),
    CONSTRAINT pk_policy PRIMARY KEY (customerid, id)
);
