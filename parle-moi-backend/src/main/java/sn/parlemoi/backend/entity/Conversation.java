package sn.parlemoi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.parlemoi.backend.enums.StatutConversation;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Code public communique a l'utilisateur (ex: PM-7K4X92) - distinct de l'id technique
    @Column(name = "code", nullable = false, unique = true, length = 12)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ecoutant_id", nullable = false)
    private Ecoutant ecoutant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutConversation statut = StatutConversation.EN_ATTENTE;

    // Position dans la file d'attente - null si pas en attente
    @Column(name = "position_file_attente")
    private Integer positionFileAttente;

    @Column(name = "sujet_optionnel", length = 500)
    private String sujetOptionnel;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @Column(name = "expire_le")
    private LocalDateTime expireLe;

    @PrePersist
    protected void onCreate() {
        this.creeLe = LocalDateTime.now();
    }
}