package sn.parlemoi.backend.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class CodeGeneratorService {

    // Alphabet volontairement sans caracteres ambigus (0/O, 1/I/L) pour eviter les erreurs de saisie utilisateur
    private static final String ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";

    // 8 caracteres (etait 6) : ce code sert aussi de secret d'acces en lecture a une conversation
    // privee (SCRUM-12) via une route publique sans autre verification - 6 caracteres (~29,7 bits
    // d'entropie, ~887M combinaisons) etait insuffisant face a une attaque par force brute distribuee.
    // 8 caracteres porte l'entropie a ~39,6 bits (~852 milliards de combinaisons).
    private static final int LONGUEUR_SUFFIXE = 8;
    private static final String PREFIXE = "PM-";

    private final SecureRandom secureRandom = new SecureRandom();

    public String genererCode() {
        StringBuilder suffixe = new StringBuilder(LONGUEUR_SUFFIXE);
        for (int i = 0; i < LONGUEUR_SUFFIXE; i++) {
            int index = secureRandom.nextInt(ALPHABET.length());
            suffixe.append(ALPHABET.charAt(index));
        }
        return PREFIXE + suffixe;
    }
}