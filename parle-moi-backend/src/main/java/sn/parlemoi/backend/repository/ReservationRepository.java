package sn.parlemoi.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.parlemoi.backend.entity.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, String> {
    Optional<Reservation> findByCode(String code);

    List<Reservation> findByDateReservation(LocalDate date);

    boolean existsByDateReservationAndHeureReservation(LocalDate date, LocalTime heure);
}