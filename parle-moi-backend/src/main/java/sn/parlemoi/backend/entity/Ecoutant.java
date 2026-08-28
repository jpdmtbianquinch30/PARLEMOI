package sn.parlemoi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.parlemoi.backend.enums.DureeRetention;
import sn.parlemoi.backend.enums.RoleEcoutant;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RoleEcoutant role = RoleEcoutant.ECOUTANT;

    @Column(name = "en_ligne", nullable = false)
    @Builder.Default
    private boolean enLigne = false;

    @Column(name = "horaire_debut")
    private LocalTime horaireDebut;

    @Column(name = "horaire_fin")
    private LocalTime horaireFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "duree_retention_messages", nullable = false)
    @Builder.Default
    private DureeRetention dureeRetentionMessages = DureeRetention.J7;

    // Verrouillage anti brute-force sur le login
    @Column(name = "tentatives_echouees", nullable = false)
    @Builder.Default
    private int tentativesEchouees = 0;

    @Column(name = "verrouille_jusqua")
    private LocalDateTime verrouilleJusqua;

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