package sn.parlemoi.backend.dto.fichier;

public record FichierUploadResponse(
        String id,
        String nomOriginal,
        String typeMime,
        long tailleOctets
) {}