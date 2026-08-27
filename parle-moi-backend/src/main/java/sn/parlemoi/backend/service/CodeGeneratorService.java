package sn.parlemoi.backend.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class CodeGeneratorService {

    // Alphabet volontairement sans caracteres ambigus (0/O, 1/I/L) pour eviter les erreurs de saisie utilisateur
    private static final String ALPHABET = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int LONGUEUR_SUFFIXE = 6;
    private static final String PREFIXE = "PM-";

    // SecureRandom obligatoire ici, pas Random - le code protege l'acces a des donnees personnelles/conversations
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