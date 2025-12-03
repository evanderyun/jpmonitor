-- Add project_id to unit_mutations for project-level profitability tracking
-- This enables tracking which project a unit was serving when it moved locations

ALTER TABLE unit_mutations 
ADD COLUMN project_id UUID REFERENCES projects(id);

CREATE INDEX idx_unit_mutations_project ON unit_mutations(project_id);

COMMENT ON COLUMN unit_mutations.project_id IS 'Project the unit was serving during this mutation (for profitability tracking)';
