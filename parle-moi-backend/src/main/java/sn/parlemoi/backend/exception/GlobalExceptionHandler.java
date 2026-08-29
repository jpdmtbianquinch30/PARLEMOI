package sn.parlemoi.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RessourceNonTrouveeException.class)
    public ResponseEntity<ErreurResponse> gererRessourceNonTrouvee(RessourceNonTrouveeException ex) {
        ErreurResponse erreur = ErreurResponse.de(404, "Ressource introuvable", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erreur);
    }

    @ExceptionHandler(ReservationConflitException.class)
    public ResponseEntity<ErreurResponse> gererConflit(ReservationConflitException ex) {
        ErreurResponse erreur = ErreurResponse.de(409, "Conflit", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erreur);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurResponse> gererValidation(MethodArgumentNotValidException ex) {
        Map<String, String> champsInvalides = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(erreurChamp ->
                champsInvalides.put(erreurChamp.getField(), erreurChamp.getDefaultMessage())
        );
        ErreurResponse erreur = ErreurResponse.deValidation(champsInvalides);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
    }
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurResponse> gererJsonMalforme(
            org.springframework.http.converter.HttpMessageNotReadableException ex
    ) {
        log.warn("Requete JSON malformee recue: {}", ex.getMessage());
        ErreurResponse erreur = ErreurResponse.de(400, "Requete invalide", "Le corps de la requete JSON est malforme");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erreur);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurResponse> gererErreurGenerique(Exception ex) {
        // On logue TOUJOURS la vraie erreur cote serveur, meme si le client ne voit qu'un message generique
        log.error("Erreur interne non geree", ex);
        ErreurResponse erreur = ErreurResponse.de(500, "Erreur interne", "Une erreur inattendue est survenue");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erreur);
    }
    @ExceptionHandler(IdentifiantsInvalidesException.class)
    public ResponseEntity<ErreurResponse> gererIdentifiantsInvalides(IdentifiantsInvalidesException ex) {
        ErreurResponse erreur = ErreurResponse.de(401, "Non autorise", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erreur);
    }

    @ExceptionHandler(CompteVerrouilleException.class)
    public ResponseEntity<ErreurResponse> gererCompteVerrouille(CompteVerrouilleException ex) {
        ErreurResponse erreur = ErreurResponse.de(423, "Compte verrouille", ex.getMessage());
        return ResponseEntity.status(HttpStatus.LOCKED).body(erreur);
    }
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErreurResponse> gererSecurite(SecurityException ex) {
        log.warn("Tentative d'acces refusee : {}", ex.getMessage());
        ErreurResponse erreur = ErreurResponse.de(401, "Non autorise", "Authentification invalide");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erreur);
    }
}