package sn.parlemoi.backend.dto.fichier;

public record FichierResponse(
        String id,
        String nomOriginal,
        String typeMime,
        long tailleOctets,
        String urlTelechargement
) {}