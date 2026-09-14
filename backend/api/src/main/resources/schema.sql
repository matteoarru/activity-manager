CREATE TABLE IF NOT EXISTS activity (
  id VARCHAR(36) PRIMARY KEY,
  activity_code VARCHAR(64) NOT NULL UNIQUE,
  course_reference VARCHAR(80) NOT NULL UNIQUE,
  title VARCHAR(240) NOT NULL,
  country_code CHAR(2) NOT NULL,
  venue VARCHAR(240) NOT NULL,
  time_zone VARCHAR(80) NOT NULL,
  starts_on DATE NOT NULL,
  ends_on DATE NOT NULL,
  status VARCHAR(32) NOT NULL,
  funding_regime VARCHAR(64) NOT NULL,
  am_username VARCHAR(120) NOT NULL,
  support_usernames VARCHAR(1000) NOT NULL DEFAULT '',
  description VARCHAR(4000) NOT NULL DEFAULT '',
  expected_participants INTEGER NOT NULL DEFAULT 0,
  invitation_modality VARCHAR(40) NOT NULL DEFAULT 'NOMINATION',
  cpl_reference VARCHAR(120),
  cpl_by_cost_type VARCHAR(4000) NOT NULL DEFAULT '{}',
  curricula_file_name VARCHAR(255),
  curricula_object_key VARCHAR(255),
  curricula_sha256 CHAR(64),
  nomination_deadline DATE,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT activity_dates_valid CHECK (ends_on >= starts_on)
);
ALTER TABLE activity ADD COLUMN IF NOT EXISTS description VARCHAR(4000) NOT NULL DEFAULT '';
ALTER TABLE activity ADD COLUMN IF NOT EXISTS expected_participants INTEGER NOT NULL DEFAULT 0;
ALTER TABLE activity ADD COLUMN IF NOT EXISTS invitation_modality VARCHAR(40) NOT NULL DEFAULT 'NOMINATION';
ALTER TABLE activity ADD COLUMN IF NOT EXISTS cpl_reference VARCHAR(120);
ALTER TABLE activity ADD COLUMN IF NOT EXISTS cpl_by_cost_type VARCHAR(4000) NOT NULL DEFAULT '{}';
ALTER TABLE activity ADD COLUMN IF NOT EXISTS curricula_file_name VARCHAR(255);
ALTER TABLE activity ADD COLUMN IF NOT EXISTS curricula_object_key VARCHAR(255);
ALTER TABLE activity ADD COLUMN IF NOT EXISTS curricula_sha256 CHAR(64);
ALTER TABLE activity ADD COLUMN IF NOT EXISTS nomination_deadline DATE;
INSERT INTO activity (id, activity_code, course_reference, title, country_code, venue, time_zone, starts_on, ends_on, status, funding_regime, am_username, support_usernames)
SELECT '00000000-0000-0000-0000-000000000001', 'ACT-DEMO-01', '2026-DEM-01', 'Operational planning demonstration', 'HU', 'Budapest', 'Europe/Budapest', DATE '2026-10-12', DATE '2026-10-16', 'INVITED', 'WORK_PROGRAMME', 'am.alex', 'po.petra,ia.ines' WHERE NOT EXISTS (SELECT 1 FROM activity WHERE course_reference = '2026-DEM-01');
INSERT INTO activity (id, activity_code, course_reference, title, country_code, venue, time_zone, starts_on, ends_on, status, funding_regime, am_username, support_usernames)
SELECT '00000000-0000-0000-0000-000000000002', 'ACT-DEMO-02', '2026-DEM-02', 'Cross-border service planning', 'BE', 'Brussels', 'Europe/Brussels', DATE '2026-11-02', DATE '2026-11-04', 'DRAFT', 'CONTRIBUTION_AGREEMENT', 'am.alex', 'ia.ines' WHERE NOT EXISTS (SELECT 1 FROM activity WHERE course_reference = '2026-DEM-02');
CREATE TABLE IF NOT EXISTS cnu (username VARCHAR(120) PRIMARY KEY, organisation VARCHAR(240) NOT NULL, country_code CHAR(2) NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE);
CREATE TABLE IF NOT EXISTS activity_invitation (activity_id VARCHAR(36) NOT NULL, cnu_username VARCHAR(120) NOT NULL, modality VARCHAR(40) NOT NULL, sent_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, sent_by VARCHAR(120) NOT NULL, PRIMARY KEY (activity_id, cnu_username), FOREIGN KEY (activity_id) REFERENCES activity(id), FOREIGN KEY (cnu_username) REFERENCES cnu(username));
CREATE TABLE IF NOT EXISTS activity_nomination (id VARCHAR(36) PRIMARY KEY, activity_id VARCHAR(36) NOT NULL REFERENCES activity(id), cnu_username VARCHAR(120) NOT NULL REFERENCES cnu(username), nominee_name VARCHAR(240) NOT NULL, selected BOOLEAN NOT NULL DEFAULT FALSE, created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP);
CREATE TABLE IF NOT EXISTS activity_cost_line (id VARCHAR(36) PRIMARY KEY, activity_id VARCHAR(36) NOT NULL REFERENCES activity(id), description VARCHAR(240) NOT NULL, paid BOOLEAN NOT NULL DEFAULT FALSE);
INSERT INTO cnu (username, organisation, country_code) SELECT 'cnu.clara', 'Synthetic National Unit A', 'FR' WHERE NOT EXISTS (SELECT 1 FROM cnu WHERE username = 'cnu.clara');
INSERT INTO cnu (username, organisation, country_code) SELECT 'cnu.niko', 'Synthetic National Unit B', 'FI' WHERE NOT EXISTS (SELECT 1 FROM cnu WHERE username = 'cnu.niko');
