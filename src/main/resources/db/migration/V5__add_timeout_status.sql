-- Migration pour ajouter le statut TIMEOUT aux runs
-- Permet de marquer les runs qui n'ont pas été terminés correctement (timeout)

-- Supprimer l'ancienne contrainte
ALTER TABLE runs DROP CONSTRAINT IF EXISTS runs_status_check;

-- Recréer la contrainte avec le nouveau statut TIMEOUT
ALTER TABLE runs ADD CONSTRAINT runs_status_check
    CHECK (status IN ('RUNNING','SUCCESS','FAILED','CANCELED','TIMEOUT'));

