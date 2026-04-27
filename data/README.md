# Data directory

Place the CQC care directory CSV here before running the seed command.

Download from: https://www.cqc.org.uk/about-us/transparency/using-cqc-data

Rename the file to `cqc_directory.csv` then run:

```bash
curl -X POST \
  "http://localhost:8080/api/ingest/csv?csvPath=/data/cqc_directory.csv&postcodePrefix=SG&postcodePrefix=AL"
```

Adjust postcode prefixes for your target area.
The file is mounted read-only into the backend container at `/data`.
