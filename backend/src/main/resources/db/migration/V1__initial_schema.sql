CREATE TABLE resource
(
    id                 UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name               VARCHAR(100)             NOT NULL,
    description        VARCHAR(500),
    location           VARCHAR(100),
    resource_type      VARCHAR(50)              NOT NULL,
    is_active          BOOLEAN                  NOT NULL DEFAULT TRUE,
    creation_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_timestamp  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);