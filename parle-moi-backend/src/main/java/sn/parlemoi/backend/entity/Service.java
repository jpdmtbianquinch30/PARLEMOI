package sn.parlemoi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(name = "description", length = 1000)
    private String description;

    // Ordre d'affichage sur la landing page
    @Column(name = "ordre_affichage")
    @Builder.Default
    private Integer ordreAffichage = 0;

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private boolean actif = true;

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Formule> formules = new ArrayList<>();

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @PrePersist
    protected void onCreate() {
        this.creeLe = LocalDateTime.now();
    }
}