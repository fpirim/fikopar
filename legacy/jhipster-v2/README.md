# jhipster-v2 (2023–)

First browser version, generated with JHipster 7.9.3: Spring Boot 2, JWT auth, Liquibase, React + Redux, PostgreSQL. Entities are in `.jhipster/`: Sicil, Cari, CariDevir, HareketSicil, HareketDetay, Stok, Katalog, CekSenet, CekSenetDetay, Makbuz.

Rows were updated in place and there was no double-entry ledger; v3 changes both (docs/adr/0003).

```bash
export JHIPSTER_SECURITY_AUTHENTICATION_JWT_BASE64_SECRET=$(openssl rand -base64 64)
./mvnw
```

JWT secrets in this snapshot are placeholders. `liquibase/fake-data/*.csv` is generated sample data. Not included: TLS keystore, docker `.env`, deploy workflow and stack, backup scripts.
