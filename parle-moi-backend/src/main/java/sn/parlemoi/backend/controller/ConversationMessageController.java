package sn.parlemoi.backend.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import sn.parlemoi.backend.dto.message.EnvoyerMessageRequest;
import sn.parlemoi.backend.service.MessageService;

import java.security.Principal;

@Controller
public class ConversationMessageController {

    private final MessageService messageService;

    public ConversationMessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/conversations/{code}/envoyer")
    public void envoyer(
            @DestinationVariable String code,
            @Payload EnvoyerMessageRequest request,
            Principal principal
    ) {
        messageService.envoyer(code, principal, request);
    }
}