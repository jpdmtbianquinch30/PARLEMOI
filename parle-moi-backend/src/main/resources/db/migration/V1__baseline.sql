-- Schema de base (tables originales du projet, avant les evolutions Phase 0)

CREATE TABLE utilisateurs (
                              id VARCHAR(255) PRIMARY KEY,
                              cree_le TIMESTAMP NOT NULL
);

CREATE TABLE ecoutants (
                           id VARCHAR(255) PRIMARY KEY,
                           email VARCHAR(255) NOT NULL UNIQUE,
                           mot_de_passe_hash VARCHAR(255) NOT NULL,
                           nom VARCHAR(255) NOT NULL,
                           en_ligne BOOLEAN NOT NULL,
                           horaire_debut TIME,
                           horaire_fin TIME,
                           duree_retention_messages VARCHAR(255) NOT NULL,
                           cree_le TIMESTAMP NOT NULL,
                           modifie_le TIMESTAMP
);

CREATE TABLE services (
                          id VARCHAR(255) PRIMARY KEY,
                          nom VARCHAR(150) NOT NULL,
                          description VARCHAR(1000),
                          ordre_affichage INTEGER,
                          actif BOOLEAN NOT NULL,
                          cree_le TIMESTAMP NOT NULL
);

CREATE TABLE formules (
                          id VARCHAR(255) PRIMARY KEY,
                          service_id VARCHAR(255) NOT NULL REFERENCES services(id),
                          nom VARCHAR(150) NOT NULL,
                          description VARCHAR(1000),
                          duree_minutes INTEGER NOT NULL,
                          prix NUMERIC(10,2) NOT NULL,
                          devise VARCHAR(10) NOT NULL,
                          ordre_affichage INTEGER,
                          actif BOOLEAN NOT NULL,
                          cree_le TIMESTAMP NOT NULL
);

CREATE TABLE conversations (
                               id VARCHAR(255) PRIMARY KEY,
                               code VARCHAR(12) NOT NULL UNIQUE,
                               utilisateur_id VARCHAR(255) NOT NULL REFERENCES utilisateurs(id),
                               ecoutant_id VARCHAR(255) NOT NULL REFERENCES ecoutants(id),
                               statut VARCHAR(255) NOT NULL,
                               position_file_attente INTEGER,
                               sujet_optionnel VARCHAR(500),
                               cree_le TIMESTAMP NOT NULL,
                               expire_le TIMESTAMP
);

CREATE TABLE messages (
                          id VARCHAR(255) PRIMARY KEY,
                          conversation_id VARCHAR(255) NOT NULL REFERENCES conversations(id),
                          auteur_type VARCHAR(20) NOT NULL,
                          contenu VARCHAR(4000) NOT NULL,
                          envoye_le TIMESTAMP NOT NULL,
                          expire_le TIMESTAMP
);

CREATE TABLE appels (
                        id VARCHAR(255) PRIMARY KEY,
                        conversation_id VARCHAR(255) NOT NULL UNIQUE REFERENCES conversations(id),
                        statut VARCHAR(255) NOT NULL,
                        started_at TIMESTAMP,
                        ended_at TIMESTAMP,
                        duree_secondes INTEGER,
                        cree_le TIMESTAMP NOT NULL
);

CREATE TABLE reservations (
                              id VARCHAR(255) PRIMARY KEY,
                              code VARCHAR(12) NOT NULL UNIQUE,
                              formule_id VARCHAR(255) NOT NULL REFERENCES formules(id),
                              date_reservation DATE NOT NULL,
                              heure_reservation TIME NOT NULL,
                              sujet_optionnel VARCHAR(500),
                              statut VARCHAR(255) NOT NULL,
                              cree_le TIMESTAMP NOT NULL,
                              annulee_le TIMESTAMP,
                              CONSTRAINT uk_reservation_date_heure UNIQUE (date_reservation, heure_reservation)
);