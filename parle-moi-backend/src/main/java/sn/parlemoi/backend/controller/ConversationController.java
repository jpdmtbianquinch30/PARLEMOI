package sn.parlemoi.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.parlemoi.backend.dto.conversation.ConversationResponse;
import sn.parlemoi.backend.dto.conversation.DemarrerConversationRequest;
import sn.parlemoi.backend.service.ConversationService;
import sn.parlemoi.backend.dto.message.HistoriqueConversationResponse;
import sn.parlemoi.backend.dto.appel.TurnCredentialsResponse;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    public ResponseEntity<ConversationResponse> demarrer(
            @Valid @RequestBody(required = false) DemarrerConversationRequest request
    ) {
        DemarrerConversationRequest corps = request != null ? request : new DemarrerConversationRequest(null);
        ConversationResponse reponse = conversationService.demarrer(corps);
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }
    @GetMapping("/{code}/turn-credentials")
    public ResponseEntity<TurnCredentialsResponse> turnCredentials(@PathVariable String code) {
        return ResponseEntity.ok(conversationService.emettreCredentialsTurn(code));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ConversationResponse> trouverParCode(@PathVariable String code) {
        return ResponseEntity.ok(conversationService.trouverParCode(code));
    }
    @GetMapping("/{code}/historique")
    public ResponseEntity<HistoriqueConversationResponse> consulterHistorique(@PathVariable String code) {
        return ResponseEntity.ok(conversationService.consulterHistorique(code));
    }
}