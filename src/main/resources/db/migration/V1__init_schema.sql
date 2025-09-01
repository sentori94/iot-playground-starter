-- Table pour les runs (lock + historique)
CREATE TABLE IF NOT EXISTS runs (
  id UUID PRIMARY KEY,
  status TEXT NOT NULL CHECK (status IN ('RUNNING','SUCCESS','FAILED','CANCELED')),
  started_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  finished_at TIMESTAMPTZ,
  params JSONB,
  error_message TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_runs_running
  ON runs ((status))
  WHERE status = 'RUNNING';

-- Table pour les données capteurs
CREATE TABLE IF NOT EXISTS sensor_data (
  id BIGSERIAL PRIMARY KEY,
  sensor_id VARCHAR(255) NOT NULL,
  type VARCHAR(255) NOT NULL,
  reading DOUBLE PRECISION NOT NULL,
  timestamp TIMESTAMPTZ NOT NULL DEFAULT now()
);
