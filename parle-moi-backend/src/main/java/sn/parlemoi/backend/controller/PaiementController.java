package sn.parlemoi.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.parlemoi.backend.dto.paiement.InitierPaiementRequest;
import sn.parlemoi.backend.dto.paiement.PaiementResponse;
import sn.parlemoi.backend.service.PaiementService;

import java.io.BufferedReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class PaiementController {

    private final PaiementService paiementService;

    public PaiementController(PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    @PostMapping("/api/conversations/{code}/paiements")
    public ResponseEntity<PaiementResponse> initier(
            @PathVariable String code,
            @Valid @RequestBody InitierPaiementRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paiementService.initier(code, request));
    }

    @PostMapping("/api/paiements/webhooks/{provider}")
    public ResponseEntity<Void> webhook(
            @PathVariable String provider,
            HttpServletRequest servletRequest
    ) throws java.io.IOException {
        String payload = lireCorpsBrut(servletRequest);
        Map<String, String> entetes = new HashMap<>();
        Collections.list(servletRequest.getHeaderNames())
                .forEach(nom -> entetes.put(nom.toLowerCase(), servletRequest.getHeader(nom)));

        paiementService.traiterWebhook(provider.toUpperCase().replace("-", "_"), payload, entetes);
        return ResponseEntity.ok().build();
    }

    // Dev uniquement - PaiementService refuse cet appel si mode-simulation=false
    @PostMapping("/api/paiements/{id}/simulateur/confirmer")
    public ResponseEntity<Void> confirmerSimule(@PathVariable String id) {
        paiementService.confirmerPaiementSimule(id);
        return ResponseEntity.ok().build();
    }

    private String lireCorpsBrut(HttpServletRequest request) throws java.io.IOException {
        StringBuilder corps = new StringBuilder();
        try (BufferedReader lecteur = request.getReader()) {
            String ligne;
            while ((ligne = lecteur.readLine()) != null) {
                corps.append(ligne);
            }
        }
        return corps.toString();
    }
}