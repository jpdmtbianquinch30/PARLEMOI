package sn.parlemoi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.parlemoi.backend.enums.StatutAppel;

import java.time.LocalDateTime;

@Entity
@Table(name = "appels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false, unique = true)
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutAppel statut = StatutAppel.EN_ATTENTE;

    // Moment reel ou l'ecoutant decroche - c'est ce qui declenche le decompte du forfait
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // Duree en secondes, calculee a la fin de l'appel (endedAt - startedAt)
    @Column(name = "duree_secondes")
    private Integer dureeSecondes;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @PrePersist
    protected void onCreate() {
        this.creeLe = LocalDateTime.now();
    }
}