package sn.parlemoi.backend.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErreurResponse(
        LocalDateTime horodatage,
        int statut,
        String erreur,
        String message,
        Map<String, String> champsInvalides
) {
    public static ErreurResponse de(int statut, String erreur, String message) {
        return new ErreurResponse(LocalDateTime.now(), statut, erreur, message, null);
    }

    public static ErreurResponse deValidation(Map<String, String> champsInvalides) {
        return new ErreurResponse(
                LocalDateTime.now(),
                400,
                "Validation echouee",
                "Un ou plusieurs champs sont invalides",
                champsInvalides
        );
    }
}