# bd-locator

A business development targeting tool for care sector outreach. Enriches CQC-registered provider and location data with geospatial scoring, Companies House contact enrichment, and LLM-powered ICP expansion — helping BD teams identify and prioritise nearby partnership opportunities.

## Use case

A BD professional enters their base location and describes their ideal partner in natural language. The system maps that description to CQC provider categories, scores nearby locations by proximity, CQC rating, service type match, and provider scale, and renders the results on a clustered map with configurable weighting.

## Data sources

- [CQC Syndication API](https://api.cqc.org.uk/public/v1) — provider and location data, updated daily
- [Companies House API](https://developer.company-information.service.gov.uk) — officer and contact enrichment
- [postcodes.io](https://postcodes.io) — postcode geocoding

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3 |
| Database | PostgreSQL 16 + PostGIS 3.4 |
| Migrations | Flyway |
| Frontend | Next.js 14, Deck.gl, NextAuth.js |
| Auth | Google OAuth 2.0 |
| LLM | Anthropic Claude (configurable) |
| Infrastructure | Docker Compose |

## Getting started

1. Clone the repo
2. Copy `infra/.env.example` to `infra/.env` and fill in credentials
3. Obtain a Google OAuth client ID and secret from [Google Cloud Console](https://console.cloud.google.com)
4. From the `infra/` directory: `docker compose up --build`
5. Navigate to `http://localhost:3000`

## Architecture

See [ARCHITECTURE.md](./ARCHITECTURE.md) for component design, data model, and ingestion pipeline documentation.

## Licence

MIT
