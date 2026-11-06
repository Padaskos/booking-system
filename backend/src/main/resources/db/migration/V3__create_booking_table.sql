CREATE TABLE booking
(
    id                  UUID                     NOT NULL PRIMARY KEY,
    resource_id         UUID                     NOT NULL,
    user_id             UUID                     NOT NULL,
    start_time          TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time            TIMESTAMP WITH TIME ZONE NOT NULL,
    booking_description VARCHAR(500),

    CONSTRAINT fk_booking_resource
        FOREIGN KEY (resource_id)
            REFERENCES resource (id),

    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT chk_booking_start_before_end
        CHECK (start_time < end_time)
);

CREATE INDEX booking_resource_time_idx
    ON booking (resource_id, start_time, end_time);