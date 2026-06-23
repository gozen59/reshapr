
-- Persist the gateway runtime version and the labels advertised at registration time.
alter table if exists gateways
    add column if not exists version varchar(255),
    add column if not exists labels JSONB;