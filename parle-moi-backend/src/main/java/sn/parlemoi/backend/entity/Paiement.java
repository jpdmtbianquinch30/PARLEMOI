package sn.parlemoi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.parlemoi.backend.enums.StatutPaiement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formule_id", nullable = false)
    private Formule formule;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montant;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String devise = "XOF";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutPaiement statut = StatutPaiement.EN_ATTENTE;

    // Wave / ORANGE_MONEY - a confirmer
    @Column(nullable = false, length = 20)
    private String provider;

    @Column(name = "reference_provider")
    private String referenceProvider;

    // Cle unique cote client pour eviter tout double-credit sur retry/webhook duplique
    @Column(name = "cle_idempotence", nullable = false, unique = true, length = 100)
    private String cleIdempotence;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @Column(name = "confirme_le")
    private LocalDateTime confirmeLe;

    @PrePersist
    protected void onCreate() {
        this.creeLe = LocalDateTime.now();
    }
}