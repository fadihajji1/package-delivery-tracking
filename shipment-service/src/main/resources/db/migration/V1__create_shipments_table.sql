CREATE TABLE shipments (
                           id BIGSERIAL PRIMARY KEY,
                           customer_id BIGINT NOT NULL,
                           status VARCHAR(30) NOT NULL,
                           origin VARCHAR(255) NOT NULL,
                           destination VARCHAR(255) NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT now(),
                           updated_at TIMESTAMP NOT NULL DEFAULT now()
);