ALTER TABLE activity ADD COLUMN IF NOT EXISTS nomination_deadline DATE;
UPDATE activity SET status = 'INVITED', nomination_deadline = starts_on
  WHERE status = 'OPEN_FOR_NOMINATION';
CREATE TABLE IF NOT EXISTS activity_nomination (
  id VARCHAR(36) PRIMARY KEY,
  activity_id VARCHAR(36) NOT NULL REFERENCES activity(id),
  cnu_username VARCHAR(120) NOT NULL REFERENCES cnu(username),
  nominee_name VARCHAR(240) NOT NULL,
  selected BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS activity_cost_line (
  id VARCHAR(36) PRIMARY KEY,
  activity_id VARCHAR(36) NOT NULL REFERENCES activity(id),
  description VARCHAR(240) NOT NULL,
  paid BOOLEAN NOT NULL DEFAULT FALSE
);
