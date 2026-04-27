-- V1__init_schema.sql
-- Initial schema for bd-locator
-- Requires PostGIS extension

CREATE EXTENSION IF NOT EXISTS postgis;

-- Legal entity registered with CQC
CREATE TABLE providers (
    provider_id         TEXT PRIMARY KEY,
    provider_name       TEXT NOT NULL,
    companies_house_no  TEXT,
    organisation_type   TEXT,
    ingested_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Physical operating location registered with CQC
CREATE TABLE locations (
    location_id         TEXT PRIMARY KEY,
    provider_id         TEXT REFERENCES providers(provider_id),
    location_name       TEXT NOT NULL,
    address_lines       TEXT,
    postcode            TEXT NOT NULL,
    coordinates         GEOMETRY(Point, 4326),
    service_types       TEXT[],
    user_bands          TEXT[],
    rating              TEXT,
    registration_status TEXT,
    ingested_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_locations_coordinates ON locations USING GIST(coordinates);
CREATE INDEX idx_locations_provider_id ON locations(provider_id);
CREATE INDEX idx_locations_postcode ON locations(postcode);

-- Companies House officer enrichment, hangs off provider
CREATE TABLE officers (
    id              BIGSERIAL PRIMARY KEY,
    provider_id     TEXT REFERENCES providers(provider_id),
    full_name       TEXT,
    role            TEXT,
    appointed_on    DATE,
    resigned_on     DATE,
    ingested_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_officers_provider_id ON officers(provider_id);

-- Watermark per source for delta ingestion
CREATE TABLE ingestion_watermarks (
    source          TEXT PRIMARY KEY,
    last_synced_at  TIMESTAMPTZ NOT NULL
);

INSERT INTO ingestion_watermarks (source, last_synced_at)
VALUES ('cqc', '2000-01-01T00:00:00Z'),
       ('companies_house', '2000-01-01T00:00:00Z');
