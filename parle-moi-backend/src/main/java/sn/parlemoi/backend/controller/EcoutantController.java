package sn.parlemoi.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sn.parlemoi.backend.dto.ecoutant.*;
import sn.parlemoi.backend.service.EcoutantService;

import java.util.List;

@RestController
@RequestMapping("/api/ecoutant")
public class EcoutantController {

    private final EcoutantService ecoutantService;

    public EcoutantController(EcoutantService ecoutantService) {
        this.ecoutantService = ecoutantService;
    }

    @GetMapping("/moi")
    public ResponseEntity<EcoutantProfilResponse> moi(Authentication authentication) {
        return ResponseEntity.ok(ecoutantService.consulterProfil(authentication.getName()));
    }

    @PutMapping("/statut-en-ligne")
    public ResponseEntity<EcoutantProfilResponse> statutEnLigne(
            Authentication authentication,
            @Valid @RequestBody MettreAJourStatutRequest request
    ) {
        return ResponseEntity.ok(ecoutantService.mettreAJourStatutEnLigne(authentication.getName(), request.enLigne()));
    }

    @PutMapping("/horaires")
    public ResponseEntity<EcoutantProfilResponse> horaires(
            Authentication authentication,
            @Valid @RequestBody MettreAJourHorairesRequest request
    ) {
        return ResponseEntity.ok(ecoutantService.mettreAJourHoraires(authentication.getName(), request));
    }

    @PutMapping("/retention")
    public ResponseEntity<EcoutantProfilResponse> retention(
            Authentication authentication,
            @Valid @RequestBody MettreAJourRetentionRequest request
    ) {
        return ResponseEntity.ok(ecoutantService.mettreAJourRetention(authentication.getName(), request));
    }

    @GetMapping("/demandes")
    public ResponseEntity<List<ConversationEcoutantResponse>> demandes(Authentication authentication) {
        return ResponseEntity.ok(ecoutantService.listerDemandes(authentication.getName()));
    }

    @PutMapping("/demandes/{code}/confirmer")
    public ResponseEntity<ConversationEcoutantResponse> confirmer(
            Authentication authentication, @PathVariable String code
    ) {
        return ResponseEntity.ok(ecoutantService.confirmer(authentication.getName(), code));
    }

    @PutMapping("/demandes/{code}/mettre-en-attente")
    public ResponseEntity<ConversationEcoutantResponse> mettreEnAttente(
            Authentication authentication, @PathVariable String code
    ) {
        return ResponseEntity.ok(ecoutantService.mettreEnAttente(authentication.getName(), code));
    }

    @PutMapping("/demandes/{code}/proposer-horaire")
    public ResponseEntity<ConversationEcoutantResponse> proposerHoraire(
            Authentication authentication, @PathVariable String code,
            @Valid @RequestBody ProposerHoraireRequest request
    ) {
        return ResponseEntity.ok(ecoutantService.proposerHoraire(authentication.getName(), code, request));
    }
}