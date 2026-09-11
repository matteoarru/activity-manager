CREATE TABLE IF NOT EXISTS activity (
  id VARCHAR(36) PRIMARY KEY,
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
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT activity_dates_valid CHECK (ends_on >= starts_on)
);
CREATE INDEX IF NOT EXISTS idx_activity_am ON activity(am_username);

INSERT INTO activity (id, course_reference, title, country_code, venue, time_zone, starts_on, ends_on, status, funding_regime, am_username, support_usernames)
SELECT '00000000-0000-0000-0000-000000000001', '2026-DEM-01', 'Operational planning demonstration', 'HU', 'Budapest', 'Europe/Budapest', DATE '2026-10-12', DATE '2026-10-16', 'OPEN_FOR_NOMINATION', 'WORK_PROGRAMME', 'am.alex', 'po.petra,ia.ines'
WHERE NOT EXISTS (SELECT 1 FROM activity WHERE course_reference = '2026-DEM-01');
INSERT INTO activity (id, course_reference, title, country_code, venue, time_zone, starts_on, ends_on, status, funding_regime, am_username, support_usernames)
SELECT '00000000-0000-0000-0000-000000000002', '2026-DEM-02', 'Cross-border service planning', 'BE', 'Brussels', 'Europe/Brussels', DATE '2026-11-02', DATE '2026-11-04', 'DRAFT', 'CONTRIBUTION_AGREEMENT', 'am.alex', 'ia.ines'
WHERE NOT EXISTS (SELECT 1 FROM activity WHERE course_reference = '2026-DEM-02');
