package sn.parlemoi.backend.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ModifierFormuleRequest(

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        String description,

        @NotNull @Min(value = 5, message = "La duree minimale est de 5 minutes")
        Integer dureeMinutes,

        @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit etre positif")
        BigDecimal prix,

        boolean actif

) {}