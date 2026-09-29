CREATE TABLE organizations (id bigserial PRIMARY KEY, name text NOT NULL);
CREATE TABLE users (id bigserial PRIMARY KEY, email text NOT NULL);
CREATE TABLE sicils (id bigserial PRIMARY KEY, organization_id bigint NOT NULL REFERENCES organizations(id),
  hesap_no text NOT NULL, hesap_adi text NOT NULL);
-- Created by db.ApplySQL and GORM before the promoted migrations run.
CREATE TABLE schema_migrations (version text PRIMARY KEY, applied_at timestamptz NOT NULL DEFAULT now());
ALTER TABLE users ADD COLUMN name text NOT NULL DEFAULT '', ADD COLUMN role text NOT NULL DEFAULT 'user';
ALTER TABLE sicils ADD COLUMN gun text NOT NULL DEFAULT '', ADD COLUMN sicil_tipi text NOT NULL DEFAULT 'FIRMA',
  ADD COLUMN iskonto double precision NOT NULL DEFAULT 0;
