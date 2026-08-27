package sn.parlemoi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.parlemoi.backend.enums.DureeRetention;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "ecoutants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ecoutant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mot_de_passe_hash", nullable = false)
    private String motDePasseHash;

    @Column(nullable = false)
    private String nom;

    // Statut en ligne / hors ligne - controle manuel par l'ecoutante
    @Column(name = "en_ligne", nullable = false)
    @Builder.Default
    private boolean enLigne = false;

    // Horaires fixes (plage possible de travail, distincte du toggle en ligne)
    @Column(name = "horaire_debut")
    private LocalTime horaireDebut;

    @Column(name = "horaire_fin")
    private LocalTime horaireFin;

    // Retention des messages - configurable par l'ecoutante
    @Enumerated(EnumType.STRING)
    @Column(name = "duree_retention_messages", nullable = false)
    @Builder.Default
    private DureeRetention dureeRetentionMessages = DureeRetention.J7;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @Column(name = "modifie_le")
    private LocalDateTime modifieLe;

    @PrePersist
    protected void onCreate() {
        this.creeLe = LocalDateTime.now();
        this.modifieLe = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifieLe = LocalDateTime.now();
    }
}