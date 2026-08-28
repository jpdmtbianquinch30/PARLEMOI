package sn.parlemoi.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.parlemoi.backend.dto.conversation.ConversationResponse;
import sn.parlemoi.backend.dto.conversation.DemarrerConversationRequest;
import sn.parlemoi.backend.service.ConversationService;

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

    @GetMapping("/{code}")
    public ResponseEntity<ConversationResponse> trouverParCode(@PathVariable String code) {
        return ResponseEntity.ok(conversationService.trouverParCode(code));
    }
}