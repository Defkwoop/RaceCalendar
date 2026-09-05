create table championship (
    id uuid primary key,
    version bigint not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null,

    code varchar(50) not null unique,
    name varchar(255) not null,
    category varchar(30) not null,
    active boolean not null default true
);

create table venue (
    id uuid primary key,
    version bigint not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null,

    slug varchar(255) not null unique,
    name varchar(255) not null,
    country_code varchar(2) not null,
    time_zone varchar(100) not null
);

create table race_meeting (
    id uuid primary key,
    version bigint not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null,

    canonical_key varchar(500) not null unique,
    name varchar(255) not null,
    venue_id uuid not null references venue(id),
    start_date date not null,
    end_date date not null
);

create table championship_round (
    id uuid primary key,
    version bigint not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null,

    championship_id uuid not null references championship(id),
    meeting_id uuid not null references race_meeting(id),
    season integer not null,
    round_number integer,
    display_name varchar(255) not null,
    weekend_format varchar(30) not null,
    source_event_id varchar(255) not null,
    official_url varchar(1000) not null,

    constraint uq_round_source
        unique (championship_id, season, source_event_id)
);

create table race_session (
    id uuid primary key,
    version bigint not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null,

    round_id uuid not null references championship_round(id),
    source_session_id varchar(255) not null,
    name varchar(255) not null,
    type varchar(30) not null,
    starts_at timestamptz not null,
    ends_at timestamptz,
    status varchar(30) not null,
    source_url varchar(1000) not null,
    last_seen_at timestamptz not null,
    last_changed_at timestamptz not null,

    constraint uq_session_source
        unique (round_id, source_session_id)
);

create index idx_race_session_starts_at
    on race_session(starts_at);

create index idx_round_season
    on championship_round(season);

create index idx_meeting_dates
    on race_meeting(start_date, end_date);