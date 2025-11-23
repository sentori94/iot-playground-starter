-- Migration pour permettre plusieurs runs simultanés
-- Suppression de la contrainte unique qui limitait à 1 seul run RUNNING

DROP INDEX IF EXISTS ux_runs_running;

