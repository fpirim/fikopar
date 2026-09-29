CREATE TABLE organizations (id bigserial PRIMARY KEY, name text NOT NULL);
CREATE TABLE users (id bigserial PRIMARY KEY, email text NOT NULL);
CREATE TABLE sicils (id bigserial PRIMARY KEY, organization_id bigint NOT NULL REFERENCES organizations(id),
  hesap_no text NOT NULL, hesap_adi text NOT NULL);
