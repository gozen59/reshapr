
create table api_tokens (
    valid_until TIMESTAMP not null,
    id varchar(255) not null,
    name varchar(255) not null,
    organization_id varchar(255) not null,
    token varchar(255) not null,
    user_id varchar(255),
    primary key (id),
    unique (organization_id, name)
);

create table artifacts (
    main_artifact boolean not null,
    content TEXT not null,
    id varchar(255) not null,
    name varchar(255) not null,
    organization_id varchar(255) not null,
    path varchar(255),
    service_id varchar(255),
    source_artifact varchar(255) not null,
    type varchar(255) check (type in ('JSON_SCHEMA','OPEN_API_SPEC','GRAPHQL_SCHEMA','PROTOBUF_SCHEMA','PROTOBUF_DESCRIPTOR','JSON_FRAGMENT','RESHAPR_PROMPTS','RESHAPR_RESOURCES','RESHAPR_CUSTOM_TOOLS')),
    primary key (id)
);

create table configuration_plans (
    id varchar(255) not null,
    name varchar(255) not null,
    description varchar(255),
    backend_endpoint varchar(255) not null,
    organization_id varchar(255) not null,
    service_id varchar(255),
    excluded_operations JSONB,
    included_operations JSONB,
    api_key varchar(255),
    initial_access_token TEXT,
    oauth2_configuration JSONB,
    backend_secret_id varchar(255),
    primary key (id)
);

create table expositions (
    id varchar(255) not null,
    created_on TIMESTAMP,
    configuration_plan_id varchar(255) not null,
    gateway_group_id varchar(255) not null,
    organization_id varchar(255) not null,
    service_id varchar(255) not null,
    primary key (id),
    unique (service_id, gateway_group_id, configuration_plan_id)
);

create table gateway_groups (
    id varchar(255) not null,
    name varchar(255),
    organization_id varchar(255) not null,
    labels JSONB,
    primary key (id)
);

create table gateways (
    last_heartbeat TIMESTAMP not null,
    started_at TIMESTAMP not null,
    id varchar(255) not null,
    name varchar(255) not null,
    organization_id varchar(255) not null,
    fqdns JSONB,
    primary key (id),
    unique (name, organization_id)
);

create table gateways_gateway_groups (
    gateway_group_id varchar(255) not null,
    gateway_id varchar(255) not null
);

create table organizations (
    description varchar(255),
    icon varchar(255),
    id varchar(255) not null,
    name varchar(255) not null unique,
    owner_id varchar(255),
    primary key (id)
);

create table secrets (
    certPem TEXT,
    description varchar(255),
    id varchar(255) not null,
    name varchar(255) not null,
    organization_id varchar(255) not null,
    password varchar(255),
    token TEXT,
    tokenHeader varchar(255),
    type varchar(255) check (type in ('ARTIFACT','ENDPOINT')),
    username varchar(255),
    use_elicitation boolean not null default false,
    oauth2_client_configuration JSONB,
    primary key (id),
    unique (organization_id, name)
);

create table services (
    created_on TIMESTAMP not null,
    id varchar(255) not null,
    name varchar(255) not null,
    organization_id varchar(255) not null,
    type varchar(255) check (type in ('REST','GRAPHQL','GRPC')),
    version varchar(255) not null,
    primary key (id),
    unique (name, version, organization_id)
);

create table services_operations (
    action varchar(255),
    input_name varchar(255),
    method varchar(255),
    name varchar(255) not null,
    output_name varchar(255),
    service_id varchar(255) not null
);

create table users (
    default_organization_id varchar(255) unique,
    email varchar(255) not null,
    firstname varchar(255),
    id varchar(255) not null,
    lastname varchar(255),
    password varchar(255),
    status varchar(255) not null check (status in ('UNCONFIRMED','REGISTERED','INACTIVE')),
    username varchar(255) unique,
    primary key (id)
);

create table users_organizations (
    members_id varchar(255) not null,
    organizations_id varchar(255) not null
);

create table quotas (
    id varchar(255) not null,
    organization_id varchar(255) not null,
    metric varchar(255),
    enabled boolean not null,
    m_limit bigint,
    remaining bigint,
    primary key (id),
    unique (metric, organization_id)
);

create table shared_resources (
    id varchar(255) not null,
    organization_id varchar(255),
    type varchar(255),
    resource_ids JSONB,
    primary key (id),
    unique (type, organization_id)
);

alter table if exists api_tokens
   add constraint FK6cw668jqb11qthoqjfwqdfsm2
   foreign key (user_id)
   references users;

alter table if exists artifacts
   add constraint FKewiwimniyuerthed3iibyrfsx
   foreign key (service_id)
   references services;

alter table if exists configuration_plans
   add constraint FKmrqwc4q30iuwvf1410whfaj0q
   foreign key (service_id)
   references services;

alter table if exists configuration_plans
   add constraint FK1mlk2w8dklqy1u40q5elyidlg
   foreign key (backend_secret_id)
   references secrets;

alter table if exists initial_access_tokens
   add constraint UK2jj1kvh2sll1u5498n8ercaws unique (iat_id);

alter table if exists expositions
   add constraint FK776lkjbb36og59cerd0pd2wmp
   foreign key (configuration_plan_id)
   references configuration_plans;

alter table if exists expositions
   add constraint FKtbf2rsbkigrsawm03nhs38uhv
   foreign key (gateway_group_id)
   references gateway_groups;

alter table if exists expositions
   add constraint FK48pfayvyy7p0g7d0f907oqf8b
   foreign key (service_id)
   references services;

alter table if exists expositions
   add constraint UK5e5xaf63guqfnuk5xnrf3rppg unique (service_id, gateway_group_id, configuration_plan_id);

alter table if exists gateways
   add constraint UKepuo5yiuj995y6i7sfnixhwqy unique (name, organization_id);

alter table if exists gateways_gateway_groups
   add constraint FKfe1887g9iu38c13sp6718lfpc
   foreign key (gateway_group_id)
   references gateway_groups;

alter table if exists gateways_gateway_groups
   add constraint FKek2ad40xb4yckusj1mywa2ntv
   foreign key (gateway_id)
   references gateways;

alter table if exists organizations
   add constraint FK525ovcw3fy6440s4o0tj8xr95
   foreign key (owner_id)
   references users;

alter table if exists organizations
   add constraint UKp9pbw3flq9hkay8hdx3ypsldy unique (name);

 alter table if exists services
   add constraint UKrug3tyt0eqeuua7nis987nb7o unique (name, version, organization_id);

alter table if exists services_operations
   add constraint FK8ldnaasnbl1kc9y2hscka43xh
   foreign key (service_id)
   references services;

alter table if exists users
   add constraint FK1dlsttc53drrgpcdybvqtyh7p
   foreign key (default_organization_id)
   references organizations;

alter table if exists users
   add constraint UKr43af9ap4edm43mmtq01oddj6 unique (username);

alter table if exists users
   add constraint UK9y2qaac3upin34bciv2av7e35 unique (default_organization_id);

alter table if exists users_organizations
   add constraint FKmrwhjups2amd8mwwtjchgnx1m
   foreign key (organizations_id)
   references organizations;

alter table if exists users_organizations
   add constraint FK3g6p5l8v8kvcc5bnh7px6p8x5
   foreign key (members_id)
   references users;

alter table if exists secrets
   add constraint UKs2j4q6n8kjdcumv78kx20n5l5 unique (organization_id, name);

alter table if exists quotas
   add constraint UK6t7qp0u5rpq1xe23l1v45dhgg unique (metric, organization_id);

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