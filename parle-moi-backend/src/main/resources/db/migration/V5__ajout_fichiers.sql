CREATE TABLE fichiers (
                          id VARCHAR(255) PRIMARY KEY,
                          conversation_id VARCHAR(255) NOT NULL REFERENCES conversations(id),
                          message_id VARCHAR(255) REFERENCES messages(id),
                          nom_original VARCHAR(255) NOT NULL,
                          type_mime VARCHAR(100) NOT NULL,
                          taille_octets BIGINT NOT NULL,
                          cle_objet VARCHAR(500) NOT NULL UNIQUE,
                          cree_le TIMESTAMP NOT NULL
);

CREATE INDEX idx_fichiers_conversation ON fichiers(conversation_id);