package sn.parlemoi.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sn.parlemoi.backend.enums.StatutReservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "reservations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reservation_date_heure",
                columnNames = {"date_reservation", "heure_reservation"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "code", nullable = false, unique = true, length = 12)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formule_id", nullable = false)
    private Formule formule;

    @Column(name = "date_reservation", nullable = false)
    private LocalDate dateReservation;

    @Column(name = "heure_reservation", nullable = false)
    private LocalTime heureReservation;

    @Column(name = "sujet_optionnel", length = 500)
    private String sujetOptionnel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutReservation statut = StatutReservation.EN_ATTENTE;

    @Column(name = "cree_le", nullable = false, updatable = false)
    private LocalDateTime creeLe;

    @Column(name = "annulee_le")
    private LocalDateTime annuleeLe;

    @PrePersist
    protected void onCreate() {
        this.creeLe = LocalDateTime.now();
    }
}