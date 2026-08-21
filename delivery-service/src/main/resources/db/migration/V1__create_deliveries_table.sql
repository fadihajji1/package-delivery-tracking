CREATE TABLE deliveries (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    agent_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL
);
