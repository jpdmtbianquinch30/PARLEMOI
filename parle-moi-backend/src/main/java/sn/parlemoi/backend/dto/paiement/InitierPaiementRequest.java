package sn.parlemoi.backend.dto.paiement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InitierPaiementRequest(

        @NotBlank(message = "L'identifiant de la formule est obligatoire")
        String formuleId,

        @NotBlank(message = "Le provider est obligatoire")
        @Pattern(regexp = "WAVE|ORANGE_MONEY", message = "Provider inconnu")
        String provider

) {}