-- Roles et securite compte sur Ecoutant
ALTER TABLE ecoutants ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ECOUTANT';
ALTER TABLE ecoutants ADD COLUMN tentatives_echouees INTEGER NOT NULL DEFAULT 0;
ALTER TABLE ecoutants ADD COLUMN verrouille_jusqua TIMESTAMP NULL;

-- Fusion progressive Reservation -> Conversation (programmation optionnelle)
ALTER TABLE conversations ADD COLUMN formule_id VARCHAR(36) NULL REFERENCES formules(id);
ALTER TABLE conversations ADD COLUMN date_programmee DATE NULL;
ALTER TABLE conversations ADD COLUMN heure_programmee TIME NULL;
ALTER TABLE conversations ADD COLUMN nb_messages_gratuits_utilises INTEGER NOT NULL DEFAULT 0;
ALTER TABLE conversations ADD COLUMN forfait_expire_le TIMESTAMP NULL;

-- Paiements dans le chat
CREATE TABLE paiements (
                           id VARCHAR(36) PRIMARY KEY,
                           conversation_id VARCHAR(36) NOT NULL REFERENCES conversations(id),
                           formule_id VARCHAR(36) NOT NULL REFERENCES formules(id),
                           montant NUMERIC(10,2) NOT NULL,
                           devise VARCHAR(10) NOT NULL DEFAULT 'XOF',
                           statut VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
                           provider VARCHAR(20) NOT NULL,
                           reference_provider VARCHAR(255) NULL,
                           cle_idempotence VARCHAR(100) NOT NULL UNIQUE,
                           cree_le TIMESTAMP NOT NULL,
                           confirme_le TIMESTAMP NULL
);

CREATE INDEX idx_paiements_conversation ON paiements(conversation_id);
CREATE INDEX idx_paiements_reference_provider ON paiements(reference_provider);