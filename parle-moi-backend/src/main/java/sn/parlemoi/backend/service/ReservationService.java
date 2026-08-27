package sn.parlemoi.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import sn.parlemoi.backend.dto.reservation.CreerReservationRequest;
import sn.parlemoi.backend.dto.reservation.ReservationResponse;
import sn.parlemoi.backend.entity.Formule;
import sn.parlemoi.backend.entity.Reservation;
import sn.parlemoi.backend.enums.StatutReservation;
import sn.parlemoi.backend.exception.ReservationConflitException;
import sn.parlemoi.backend.exception.RessourceNonTrouveeException;
import sn.parlemoi.backend.repository.FormuleRepository;
import sn.parlemoi.backend.repository.ReservationRepository;

@Service
public class ReservationService {

    private static final int MAX_TENTATIVES_GENERATION_CODE = 5;

    private final ReservationRepository reservationRepository;
    private final FormuleRepository formuleRepository;
    private final CodeGeneratorService codeGeneratorService;

    public ReservationService(
            ReservationRepository reservationRepository,
            FormuleRepository formuleRepository,
            CodeGeneratorService codeGeneratorService
    ) {
        this.reservationRepository = reservationRepository;
        this.formuleRepository = formuleRepository;
        this.codeGeneratorService = codeGeneratorService;
    }

    @Transactional
    public ReservationResponse creerReservation(CreerReservationRequest request) {

        Formule formule = formuleRepository.findById(request.formuleId())
                .filter(Formule::isActif)
                .orElseThrow(() -> new RessourceNonTrouveeException("Formule introuvable ou inactive"));

        // Verification rapide avant ecriture - la vraie garantie reste la contrainte unique en base
        boolean creneauDejaPris = reservationRepository.existsByDateReservationAndHeureReservation(
                request.dateReservation(),
                request.heureReservation()
        );
        if (creneauDejaPris) {
            throw new ReservationConflitException("Ce creneau n'est plus disponible");
        }

        Reservation reservation = Reservation.builder()
                .code(genererCodeUnique())
                .formule(formule)
                .dateReservation(request.dateReservation())
                .heureReservation(request.heureReservation())
                .sujetOptionnel(request.sujetOptionnel())
                .statut(StatutReservation.EN_ATTENTE)
                .build();

        try {
            Reservation sauvegardee = reservationRepository.save(reservation);
            return versReponse(sauvegardee);
        } catch (DataIntegrityViolationException e) {
            // Filet de securite en cas de race condition passee entre le check et l'ecriture
            throw new ReservationConflitException("Ce creneau vient d'etre pris par quelqu'un d'autre");
        }
    }

    @Transactional
    public ReservationResponse trouverParCode(String code) {
        Reservation reservation = reservationRepository.findByCode(code)
                .orElseThrow(() -> new RessourceNonTrouveeException("Aucune reservation trouvee avec ce code"));
        return versReponse(reservation);
    }

    @Transactional
    public ReservationResponse annulerParCode(String code) {
        Reservation reservation = reservationRepository.findByCode(code)
                .orElseThrow(() -> new RessourceNonTrouveeException("Aucune reservation trouvee avec ce code"));

        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new ReservationConflitException("Cette reservation est deja annulee");
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        reservation.setAnnuleeLe(java.time.LocalDateTime.now());

        Reservation sauvegardee = reservationRepository.save(reservation);
        return versReponse(sauvegardee);
    }

    private String genererCodeUnique() {
        for (int i = 0; i < MAX_TENTATIVES_GENERATION_CODE; i++) {
            String code = codeGeneratorService.genererCode();
            if (reservationRepository.findByCode(code).isEmpty()) {
                return code;
            }
        }
        throw new IllegalStateException("Impossible de generer un code unique apres plusieurs tentatives");
    }

    private ReservationResponse versReponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getCode(),
                reservation.getFormule().getNom(),
                reservation.getDateReservation(),
                reservation.getHeureReservation(),
                reservation.getStatut()
        );
    }
}