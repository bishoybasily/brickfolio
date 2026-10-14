-- Initial schema. Hibernate runs with ddl-auto=validate, so this must match the JPA entities.

create table developers (
    id   varchar(255) not null,
    name varchar(255),
    constraint pk_developers primary key (id)
);

create table projects (
    id                   varchar(255) not null,
    name                 varchar(255),
    location             varchar(255),
    location_description text,
    delivery_months      integer,
    developer_id         varchar(255),
    constraint pk_projects primary key (id),
    constraint fk_projects_developer foreign key (developer_id) references developers (id)
);

create index ix_projects_developer_id on projects (developer_id);

create table units (
    id           varchar(255) not null,
    name         varchar(255),
    indoor_area  float(53),
    outdoor_area float(53),
    floor        integer,
    price        float(53),
    type         varchar(255),
    project_id   varchar(255),
    constraint pk_units primary key (id),
    constraint fk_units_project foreign key (project_id) references projects (id),
    constraint ck_units_type check (type in ('RETAIL', 'APARTMENT', 'OFFICE', 'COMMERCIAL'))
);

create index ix_units_project_id on units (project_id);

create table payment_plans (
    id                  varchar(255) not null,
    months              integer,
    discount_percentage float(53),
    project_id          varchar(255),
    constraint pk_payment_plans primary key (id),
    constraint fk_payment_plans_project foreign key (project_id) references projects (id)
);

create index ix_payment_plans_project_id on payment_plans (project_id);

create table payment_plan_payments (
    payment_plan_id varchar(255) not null,
    name            varchar(255),
    due             integer,
    percentage      float4,
    constraint uq_payment_plan_payments unique (payment_plan_id, due, name, percentage),
    constraint fk_payment_plan_payments_payment_plan foreign key (payment_plan_id) references payment_plans (id)
);

create table conversations (
    id varchar(255) not null,
    constraint pk_conversations primary key (id)
);

create table conversation_messages (
    conversation_id varchar(255) not null,
    message_order   integer      not null,
    type            varchar(255),
    content         text,
    timestamp       timestamp(6) with time zone,
    constraint pk_conversation_messages primary key (conversation_id, message_order),
    constraint fk_conversation_messages_conversation foreign key (conversation_id) references conversations (id),
    constraint ck_conversation_messages_order check (message_order >= 0)
);
