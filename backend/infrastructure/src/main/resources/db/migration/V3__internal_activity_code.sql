ALTER TABLE activity ADD COLUMN IF NOT EXISTS activity_code VARCHAR(64);
UPDATE activity SET activity_code = CONCAT('ACT-', id) WHERE activity_code IS NULL;
ALTER TABLE activity ALTER COLUMN activity_code SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_activity_internal_code ON activity(activity_code);
