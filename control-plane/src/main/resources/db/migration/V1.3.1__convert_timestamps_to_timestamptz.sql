-- Convert naive TIMESTAMP columns to TIMESTAMP WITH TIME ZONE (timestamptz) so that
-- the control plane REST APIs expose RFC3339 compliant dates (with offset).
--
-- Existing values were persisted from LocalDateTime/OffsetDateTime without an explicit
-- offset; they are interpreted here as UTC. Adapt the 'UTC' time zone below if your
-- previous deployment wrote timestamps in another zone.

-- The active_expositions view depends on expositions.created_on, so it must be dropped
-- before altering the column type and recreated afterwards.
drop view if exists active_expositions;

alter table api_tokens
    alter column valid_until type timestamptz using valid_until at time zone 'UTC';

alter table services
    alter column created_on type timestamptz using created_on at time zone 'UTC';

alter table expositions
    alter column created_on type timestamptz using created_on at time zone 'UTC';

alter table gateways
    alter column started_at type timestamptz using started_at at time zone 'UTC',
    alter column last_heartbeat type timestamptz using last_heartbeat at time zone 'UTC';

alter table service_accounts
    alter column valid_until type timestamptz using valid_until at time zone 'UTC';

-- Recreate the active_expositions view (identical to V1.0.0 definition).
create or replace view active_expositions as
   select
      e.id,
      e.organization_id,
      e.created_on,
      s.id as service_id,
      s.name as service_name,
      s.version as service_version,
      s.type as service_type,
      c.id as config_id,
      c.backend_endpoint as config_backend_endpoint,
      json_agg(JSON_BUILD_OBJECT('id', g.id, 'name', g.name, 'fqdns', g.fqdns)) as gateways
   from expositions e
      left join services s
         on e.service_id = s.id
      inner join configuration_plans c
         on e.configuration_plan_id = c.id
      left join gateway_groups gg
         on e.gateway_group_id = gg.id
      inner join gateways_gateway_groups ggg
         on gg.id = ggg.gateway_group_id
      left join gateways g
         on ggg.gateway_id = g.id
   group by e.id, s.id, c.id;

