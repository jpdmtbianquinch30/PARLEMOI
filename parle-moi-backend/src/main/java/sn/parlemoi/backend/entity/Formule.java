package sn.parlemoi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "formules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Formule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(name = "description", length = 1000)
    private String description;

    // Duree en minutes de la formule (utilisee pour le decompte du forfait a l'appel)
    @Column(name = "duree_minutes", nullable = false)
    private Integer dureeMinutes;

    // Prix - BigDecimal obligatoire pour l'argent, jamais de double/float
    @Column(name = "prix", nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(name = "devise", nullable = false, length = 10)
    @Builder.Default
    private String devise = "XOF";

    @Column(name = "ordre_affichage")
    @Builder.Default
    private Integer ordreAffichage = 0;

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private boolean actif = true;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @PrePersist
    protected void onCreate() {
        this.creeLe = LocalDateTime.now();
    }
}