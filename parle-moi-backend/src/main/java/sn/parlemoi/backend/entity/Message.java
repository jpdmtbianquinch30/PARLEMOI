package sn.parlemoi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    // "UTILISATEUR" ou "ECOUTANT" - pas de lien direct vers une identite dans le corps du message
    @Column(name = "auteur_type", nullable = false, length = 20)
    private String auteurType;

    @Column(name = "contenu", nullable = false, length = 4000)
    private String contenu;

    @Column(name = "envoye_le", nullable = false, updatable = false)
    private LocalDateTime envoyeLe;

    // Date de suppression programmee, calculee a la creation selon la duree de retention de l'ecoutant
    @Column(name = "expire_le")
    private LocalDateTime expireLe;

    @PrePersist
    protected void onCreate() {
        this.envoyeLe = LocalDateTime.now();
    }
}