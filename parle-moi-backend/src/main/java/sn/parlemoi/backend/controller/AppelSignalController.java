package sn.parlemoi.backend.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import sn.parlemoi.backend.dto.appel.SignalAppelRequest;
import sn.parlemoi.backend.service.AppelService;

import java.security.Principal;

@Controller
public class AppelSignalController {

    private final AppelService appelService;

    public AppelSignalController(AppelService appelService) {
        this.appelService = appelService;
    }

    @MessageMapping("/conversations/{code}/appel/signal")
    public void signaler(
            @DestinationVariable String code,
            @Payload SignalAppelRequest request,
            Principal principal
    ) {
        appelService.traiterSignal(code, principal, request);
    }
}