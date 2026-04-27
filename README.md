# BD Locator

> A geospatial business development targeting tool for the care sector, built to answer a specific operational question: *which regulated care providers near our location are the most valuable partnership opportunities, and why?*

---

## The problem

Business development in the care sector is harder than it looks. The UK has over 120,000 regulated care locations, each with a legal entity behind it, a CQC inspection history, a geographic footprint, and a patient population. A BD professional trying to identify partnership opportunities near a new site is typically working from a mix of Google searches, word-of-mouth referrals, and manual spreadsheet triage.

The data exists to do this properly — the Care Quality Commission publishes a full register of regulated providers, updated daily, with ratings, service types, and location data. Companies House holds the corporate structure behind each provider. What's been missing is a system that joins these sources together, applies a scoring model that reflects what actually matters to a BD team, and presents the results in a way that drives action.

BD Locator is that system.

---

## What it does

A BD user enters their base postcode and describes their ideal partner in natural language. The system:

1. **Geocodes** the postcode and queries all CQC-registered locations within the configured radius using PostGIS spatial indexing
2. **Expands** the natural language description into CQC service taxonomy categories via an LLM — so "memory care homes run by larger groups" becomes a structured filter against the real CQC classification system
3. **Scores** each qualifying location across three configurable dimensions: proximity (exponential decay), CQC rating (normalised ordinal), and provider scale (sigmoid over location count)
4. **Renders** results on a live map with colour-coded markers, a ranked results panel, and one-click actions — directions, address copy, Companies House lookup, web search

The scoring weights are adjustable per search. The ICP filter acts as a gate, not a dimension — a provider that doesn't match the partner profile at all is excluded rather than ranked low.

---

## Why the architecture looks the way it does

Several decisions here were deliberate rather than incidental.

**Query-time scoring, not pre-computed.** Storing scores would be faster but creates a cache invalidation problem the moment a BD adjusts a weight. With a geographically filtered candidate set — typically a few hundred locations, not 120,000 — scoring at query time is fast enough and always reflects current settings.

**CQC locations as the anchor, not Companies House.** The CQC register is the authoritative source for where care is actually delivered. Companies House is richer on corporate structure but its registered address is often a solicitor's office or a holding company, not the location a BD would visit. The schema treats CH enrichment as opportunistic — useful when it joins cleanly, silent when it doesn't.

**LLM as a taxonomy bridge, not a search engine.** The ICP expansion feature doesn't let the LLM search freely. It maps natural language onto a known, bounded taxonomy — the actual service type and specialism strings in the database. This makes the expansion deterministic enough to be useful without hallucinating categories that don't exist. The system prompt includes the real taxonomy values extracted from the database.

**CSV seed for discovery, API for enrichment.** The CQC Syndication API doesn't support efficient geographic filtering on the list endpoints — only detail endpoints return full data. The approach here uses CQC's monthly care directory CSV for location discovery and postcode filtering, then calls the API per location ID for full detail. This is the pattern CQC's own documentation implies, and it's meaningfully faster than paginating the full dataset.

**Graceful degradation throughout.** A location with no coordinates still lands in the store. A provider with no Companies House record doesn't block ingestion. An LLM call that fails returns an empty keyword set, which means no gate is applied rather than no results returned. These aren't edge cases — they're the normal operating conditions of real public sector data.

---

## Technical overview

```
┌──────────────────────────────────────────────────────┐
│  Next.js 16 (App Router)                             │
│  Deck.gl H3 map · Weight sliders · Result cards      │
│  Google OAuth via NextAuth v5                        │
└────────────────────┬─────────────────────────────────┘
                     │ REST / JSON
┌────────────────────▼─────────────────────────────────┐
│  Spring Boot 3.2 (Java 21)                           │
│  ├── Search API  — PostGIS spatial query + scoring   │
│  ├── Ingest API  — CQC pipeline (CSV + delta sync)   │
│  └── LLM adapter — ICP expansion (Anthropic Claude)  │
└────────────────────┬─────────────────────────────────┘
                     │
┌────────────────────▼─────────────────────────────────┐
│  PostgreSQL 16 + PostGIS 3.4                         │
│  providers · locations · officers · watermarks       │
└──────────────────────────────────────────────────────┘
```

| Layer | Technology | Why |
|---|---|---|
| Backend | Java 21 / Spring Boot 3.2 | Production-grade JVM stack; familiar to enterprise RevOps engineering teams |
| Database | PostgreSQL + PostGIS | First-class geospatial indexing; `ST_DWithin` with geography casting handles Earth curvature correctly at UK scales |
| Migrations | Flyway | Schema versioning as code; reproducible from zero |
| Frontend | Next.js 16 / Deck.gl | WebGL-accelerated map rendering; handles large point datasets without performance degradation |
| Auth | Google OAuth / NextAuth v5 | Zero credential management for the end user; appropriate for a single-tenant demo tool |
| LLM | Anthropic Claude via adapter | Provider-swappable; Haiku tier keeps latency low for a synchronous ICP expansion call |
| Infra | Docker Compose | Single-command boot; no infrastructure dependencies beyond Docker Desktop |

### Scoring model

Each location is scored across three dimensions at query time:

```
score = w₁ · proximity + w₂ · rating + w₃ · scale

proximity = exp(−distance_km / halflife_km)
rating    = {Outstanding: 1.0, Good: 0.75, RI: 0.4, Inadequate: 0.1, unrated: 0.5}
scale     = 1 − exp(−location_count / scale_factor)
```

Weights are normalised from user-set sliders (1–10 scale) so they always sum to 1.0. The ICP gate runs before scoring — locations that don't substring-match the expanded keyword set are excluded entirely rather than ranked last.

### Ingestion pipeline

```
CQC care directory CSV
        │
        ▼ filter by postcode prefix
  [location IDs]
        │
        ▼ GET /locations/{id}  ──→  CQC Syndication API
  [full location detail]              (subscription key auth)
        │
        ▼ upsert
  PostgreSQL (PostGIS)
        │
        ▼ advance watermark
  [delta sync ready]
```

Subsequent runs use the CQC Changes endpoint from the stored watermark — only modified records are re-fetched.

---

## Data sources

| Source | What it provides | Access |
|---|---|---|
| [CQC Syndication API](https://api-portal.service.cqc.org.uk) | Full provider and location detail, ratings, coordinates | Subscription key (free registration) |
| [CQC Care Directory](https://www.cqc.org.uk/about-us/transparency/using-cqc-data) | Bulk location list for regional discovery | Open Government Licence |
| [Companies House](https://find-and-update.company-information.service.gov.uk) | Officer and contact enrichment | Public API |
| [postcodes.io](https://postcodes.io) | Postcode geocoding fallback | Open / free |

---

## Getting started

### Prerequisites

- Docker Desktop
- Google Cloud OAuth 2.0 credentials ([guide](https://developers.google.com/identity/protocols/oauth2)) — add `http://localhost:3000/api/auth/callback/google` as an authorised redirect URI
- CQC Developer Portal subscription key ([api-portal.service.cqc.org.uk](https://api-portal.service.cqc.org.uk))
- Mapbox public token ([account.mapbox.com](https://account.mapbox.com)) — free tier
- Anthropic API key ([console.anthropic.com](https://console.anthropic.com))

### Setup

```bash
git clone https://github.com/your-username/bd-locator.git
cd bd-locator

# Configure environment
cp infra/.env.example infra/.env
# Fill in infra/.env — see comments in the file

# Generate AUTH_SECRET
openssl rand -base64 32

# Start the stack
cd infra && docker compose up --build
```

### Seed data

Download the CQC care directory from the [CQC transparency page](https://www.cqc.org.uk/about-us/transparency/using-cqc-data), rename it `cqc_directory.csv`, and place it in the `data/` directory.

```bash
# Seed for a target area — adjust postcode prefixes as needed
# SG = Stevenage/Hitchin/Letchworth, AL = St Albans/Harpenden
curl -X POST \
  "http://localhost:8080/api/ingest/csv?csvPath=/data/cqc_directory.csv&postcodePrefix=SG&postcodePrefix=AL"
```

Allow 10–20 minutes for a typical regional set (~600 locations). Progress is logged every 50 records.

Navigate to [http://localhost:3000](http://localhost:3000) and sign in with Google.

---

## Known limitations and honest trade-offs

- **CQC list endpoint filtering** — the CQC Syndication API does not reliably support geographic filtering on the list endpoints. The CSV-based discovery approach is a deliberate workaround, not a gap.
- **Companies House join rate** — NHS bodies, charities, and sole traders frequently have no clean Companies House record. The schema treats CH enrichment as nullable throughout; the BD user sees what's available.
- **ICP gate precision** — the LLM expansion maps natural language onto CQC taxonomy with reasonable fidelity, but the system is not a semantic search engine. Unusual or highly specific ICP descriptions may under-match.
- **Single-tenant auth** — Google OAuth is configured for single-user demo use. Multi-tenant deployment would require session isolation and per-user weight persistence.

---

## Licence

MIT
