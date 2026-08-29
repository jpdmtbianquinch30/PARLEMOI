package sn.parlemoi.backend.enums;

import java.util.Set;

public final class TypeFichierAutorise {

    // Whitelist stricte de types MIME reels autorises - jamais se fier a l'extension du nom de fichier,
    // qui peut etre falsifiee. La verification du type reel se fait via detection de contenu (Phase 6B).
    public static final Set<String> AUTORISES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "video/mp4",
            "video/webm",
            "application/pdf"
    );

    private TypeFichierAutorise() {}
}