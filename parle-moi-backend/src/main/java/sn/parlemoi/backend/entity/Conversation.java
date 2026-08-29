package sn.parlemoi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.parlemoi.backend.enums.EtatNotificationForfait;
import sn.parlemoi.backend.enums.StatutConversation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @Column(name = "position_file_attente")
    private Integer positionFileAttente;

    @Column(name = "sujet_optionnel", length = 500)
    private String sujetOptionnel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formule_id")
    private Formule formule;

    @Column(name = "date_programmee")
    private LocalDate dateProgrammee;

    @Column(name = "heure_programmee")
    private LocalTime heureProgrammee;

    @Column(name = "nb_messages_gratuits_utilises", nullable = false)
    @Builder.Default
    private int nbMessagesGratuitsUtilises = 0;

    @Column(name = "forfait_expire_le")
    private LocalDateTime forfaitExpireLe;

    // Empeche le job de surveillance de renvoyer le meme avertissement/notification en boucle
    // toutes les 30s pendant toute la fenetre concernee - reinitialise a chaque nouveau paiement reussi.
    @Enumerated(EnumType.STRING)
    @Column(name = "etat_notification_forfait", nullable = false, length = 30)
    @Builder.Default
    private EtatNotificationForfait etatNotificationForfait = EtatNotificationForfait.AUCUNE;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @Column(name = "expire_le")
    private LocalDateTime expireLe;

    @PrePersist
    protected void onCreate() {
        this.creeLe = LocalDateTime.now();
    }
}