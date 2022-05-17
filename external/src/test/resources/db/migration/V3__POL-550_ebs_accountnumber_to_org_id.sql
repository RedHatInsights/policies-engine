ALTER TABLE account_latest_update
  ADD COLUMN org_id text;

CREATE INDEX ix_account_latest_update_org_id ON public.account_latest_update (org_id);

ALTER TABLE policy
  ADD COLUMN org_id text;

CREATE INDEX ix_policy_org_id ON public.policy (org_id);
