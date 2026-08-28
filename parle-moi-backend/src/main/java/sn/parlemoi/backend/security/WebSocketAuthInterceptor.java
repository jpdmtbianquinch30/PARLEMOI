package sn.parlemoi.backend.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import sn.parlemoi.backend.entity.Conversation;
import sn.parlemoi.backend.enums.RoleEcoutant;
import sn.parlemoi.backend.repository.ConversationRepository;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    // N'autorise QUE les destinations conversations - tout le reste est bloque
    // par principe du moindre privilege (meme logique que SecurityConfig cote REST)
    private static final Pattern DESTINATION_CONVERSATION =
            Pattern.compile("^/(app|topic)/conversations/([A-Za-z0-9-]+)(/.*)?$");

    private final JwtService jwtService;
    private final ConversationRepository conversationRepository;

    public WebSocketAuthInterceptor(JwtService jwtService, ConversationRepository conversationRepository) {
        this.jwtService = jwtService;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            accessor.setUser(authentifier(accessor));
            return message;
        }

        if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            autoriserAccesDestination(accessor);
        }

        return message;
    }

    private Principal authentifier(StompHeaderAccessor accessor) {
        String enTeteAuth = premierHeader(accessor, "Authorization");
        if (enTeteAuth != null && enTeteAuth.startsWith("Bearer ")) {
            String token = enTeteAuth.substring(7);
            if (!jwtService.estValide(token)) {
                throw new MessagingException("Token invalide ou expire");
            }
            String ecoutantId = jwtService.extraireEcoutantId(token);
            String role = jwtService.extraireRole(token);
            return new EcoutantPrincipal(ecoutantId, RoleEcoutant.valueOf(role));
        }

        String code = premierHeader(accessor, "conversationCode");
        if (code != null && !code.isBlank()) {
            Conversation conversation = conversationRepository.findByCode(code)
                    .orElseThrow(() -> new MessagingException("Conversation introuvable"));
            if (conversation.getExpireLe() != null && conversation.getExpireLe().isBefore(LocalDateTime.now())) {
                throw new MessagingException("Cette conversation a expire");
            }
            return new AnonymePrincipal(code);
        }

        throw new MessagingException("Authentification requise pour se connecter au chat");
    }

    private void autoriserAccesDestination(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }

        Matcher matcher = DESTINATION_CONVERSATION.matcher(destination);
        if (!matcher.matches()) {
            throw new MessagingException("Destination non autorisee");
        }

        String codeDestination = matcher.group(2);
        Principal principal = accessor.getUser();

        if (principal instanceof AnonymePrincipal anonyme) {
            if (!anonyme.code().equals(codeDestination)) {
                throw new MessagingException("Acces refuse a cette conversation");
            }
            return;
        }

        if (principal instanceof EcoutantPrincipal ecoutant) {
            if (ecoutant.role() == RoleEcoutant.ADMIN) {
                return; // supervision globale - justifiee, prevue par SCRUM-19
            }
            boolean autorise = conversationRepository.existsByCodeAndEcoutantId(codeDestination, ecoutant.ecoutantId());
            if (!autorise) {
                throw new MessagingException("Cette conversation ne vous est pas assignee");
            }
            return;
        }

        throw new MessagingException("Principal non reconnu");
    }

    private String premierHeader(StompHeaderAccessor accessor, String nom) {
        List<String> valeurs = accessor.getNativeHeader(nom);
        return (valeurs == null || valeurs.isEmpty()) ? null : valeurs.get(0);
    }
}