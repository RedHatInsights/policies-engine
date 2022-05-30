CREATE TABLE org_id_latest_update (
    org_id TEXT NOT NULL,
    latest TIMESTAMP NOT NULL,
    CONSTRAINT pk_org_id_latest_update PRIMARY KEY (org_id)
);

ALTER TABLE policy
  ADD COLUMN org_id text;

CREATE INDEX ix_policy_org_id ON public.policy (org_id);