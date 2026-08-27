package sn.parlemoi.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.parlemoi.backend.dto.reservation.CreerReservationRequest;
import sn.parlemoi.backend.dto.reservation.ReservationResponse;
import sn.parlemoi.backend.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> creerReservation(
            @Valid @RequestBody CreerReservationRequest request
    ) {
        ReservationResponse reponse = reservationService.creerReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    @GetMapping("/{code}")
    public ResponseEntity<ReservationResponse> trouverParCode(@PathVariable String code) {
        ReservationResponse reponse = reservationService.trouverParCode(code);
        return ResponseEntity.ok(reponse);
    }

    @PostMapping("/{code}/annuler")
    public ResponseEntity<ReservationResponse> annulerParCode(@PathVariable String code) {
        ReservationResponse reponse = reservationService.annulerParCode(code);
        return ResponseEntity.ok(reponse);
    }
}