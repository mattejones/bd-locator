-- V2__seed_test_data.sql
-- Realistic test data for local development and scoring validation.
-- Covers a mix of provider sizes, CQC ratings, and service types
-- in the Slough / Windsor / Berkshire area.
-- Remove this migration before any production deployment.

-- -------------------------------------------------------------------------
-- Providers
-- -------------------------------------------------------------------------

INSERT INTO providers (provider_id, provider_name, companies_house_no, organisation_type, ingested_at, updated_at)
VALUES
    ('1-TEST-SUNRISE',    'Sunrise Care Group Ltd',              '12345678', 'Social Care Org',  now(), now()),
    ('1-TEST-WILLOWBRK',  'Willowbrook Care Services',           NULL,       'Voluntary Org',    now(), now()),
    ('1-TEST-PREMIER',    'Premier Home Care (Berkshire) Ltd',   '87654321', 'Social Care Org',  now(), now()),
    ('1-TEST-AUTUMN',     'Autumn Years Healthcare Ltd',         '11223344', 'Social Care Org',  now(), now());

-- -------------------------------------------------------------------------
-- Locations  (coordinates: ST_MakePoint(longitude, latitude))
-- -------------------------------------------------------------------------

INSERT INTO locations (
    location_id, provider_id, location_name, address_lines, postcode,
    coordinates, service_types, user_bands, rating, registration_status,
    ingested_at, updated_at
)
VALUES
    (
        '1-LOC-SUN-01', '1-TEST-SUNRISE',
        'Sunrise Court Care Home',
        'Sunrise Court, High Street, Slough', 'SL1 1LH',
        ST_SetSRID(ST_MakePoint(-0.5950, 51.5105), 4326),
        ARRAY['Accommodation for persons who require nursing or personal care'],
        ARRAY['Older Adults', 'Dementia'],
        'Outstanding', 'Active', now(), now()
    ),
    (
        '1-LOC-SUN-02', '1-TEST-SUNRISE',
        'Sunrise Gardens Residential',
        '14 Castle Hill, Windsor', 'SL4 1HN',
        ST_SetSRID(ST_MakePoint(-0.6077, 51.4839), 4326),
        ARRAY['Accommodation for persons who require nursing or personal care'],
        ARRAY['Older Adults'],
        'Good', 'Active', now(), now()
    ),
    (
        '1-LOC-SUN-03', '1-TEST-SUNRISE',
        'Sunrise View Residential Home',
        '3 Farnham Road, Slough', 'SL2 1HP',
        ST_SetSRID(ST_MakePoint(-0.5870, 51.5220), 4326),
        ARRAY['Accommodation for persons who require nursing or personal care'],
        ARRAY['Older Adults', 'Dementia', 'Physical Disabilities'],
        'Good', 'Active', now(), now()
    ),
    (
        '1-LOC-WBK-01', '1-TEST-WILLOWBRK',
        'Willowbrook Day Services',
        '22 Chalvey Road, Slough', 'SL1 2SR',
        ST_SetSRID(ST_MakePoint(-0.6020, 51.5080), 4326),
        ARRAY['Community based activities for people with mental health needs', 'Personal care'],
        ARRAY['Older Adults', 'Mental Health'],
        'Good', 'Active', now(), now()
    ),
    (
        '1-LOC-WBK-02', '1-TEST-WILLOWBRK',
        'Willowbrook Domiciliary Care',
        '8 Langley Road, Slough', 'SL3 7HB',
        ST_SetSRID(ST_MakePoint(-0.5240, 51.5010), 4326),
        ARRAY['Personal care'],
        ARRAY['Older Adults'],
        'Requires Improvement', 'Active', now(), now()
    ),
    (
        '1-LOC-PRE-01', '1-TEST-PREMIER',
        'Premier Home Care Slough',
        '55 Windsor Road, Slough', 'SL1 2EE',
        ST_SetSRID(ST_MakePoint(-0.5880, 51.5090), 4326),
        ARRAY['Personal care'],
        ARRAY['Older Adults', 'Dementia'],
        'Good', 'Active', now(), now()
    ),
    (
        '1-LOC-AUT-01', '1-TEST-AUTUMN',
        'Autumn Years Nursing Home',
        '1 Stanwell Road, Stanwell', 'TW19 7JL',
        ST_SetSRID(ST_MakePoint(-0.4720, 51.4510), 4326),
        ARRAY['Accommodation for persons who require nursing or personal care', 'Nursing care'],
        ARRAY['Older Adults', 'Dementia', 'Physical Disabilities'],
        'Outstanding', 'Active', now(), now()
    ),
    (
        '1-LOC-AUT-02', '1-TEST-AUTUMN',
        'Autumn Years Windsor',
        '9 Alma Road, Windsor', 'SL4 3HJ',
        ST_SetSRID(ST_MakePoint(-0.6190, 51.4760), 4326),
        ARRAY['Accommodation for persons who require nursing or personal care'],
        ARRAY['Older Adults'],
        'Good', 'Active', now(), now()
    ),
    (
        '1-LOC-AUT-03', '1-TEST-AUTUMN',
        'Autumn Years Maidenhead',
        '34 Queen Street, Maidenhead', 'SL6 1HZ',
        ST_SetSRID(ST_MakePoint(-0.7230, 51.5220), 4326),
        ARRAY['Accommodation for persons who require nursing or personal care', 'Nursing care'],
        ARRAY['Older Adults', 'Dementia'],
        'Good', 'Active', now(), now()
    );

-- -------------------------------------------------------------------------
-- Officers (only for providers with a CH number)
-- -------------------------------------------------------------------------

INSERT INTO officers (provider_id, full_name, role, appointed_on, resigned_on, ingested_at)
VALUES
    ('1-TEST-SUNRISE', 'Sarah Mitchell',    'Director',              '2015-03-12', NULL,         now()),
    ('1-TEST-SUNRISE', 'David Okafor',      'Director',              '2018-07-01', NULL,         now()),
    ('1-TEST-SUNRISE', 'Janet Brownlee',    'Secretary',             '2015-03-12', '2022-01-31', now()),
    ('1-TEST-PREMIER', 'Mark Hendersen',    'Director',              '2019-11-20', NULL,         now()),
    ('1-TEST-AUTUMN',  'Caroline Firth',    'Chief Executive',       '2012-04-05', NULL,         now()),
    ('1-TEST-AUTUMN',  'Robert Tan',        'Director',              '2016-09-14', NULL,         now()),
    ('1-TEST-AUTUMN',  'Patricia Obinna',   'Non-Executive Director','2020-02-28', NULL,         now());
