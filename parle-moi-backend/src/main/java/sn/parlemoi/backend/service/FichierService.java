package sn.parlemoi.backend.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sn.parlemoi.backend.dto.fichier.FichierUploadResponse;
import sn.parlemoi.backend.entity.Conversation;
import sn.parlemoi.backend.entity.Fichier;
import sn.parlemoi.backend.enums.TypeFichierAutorise;
import sn.parlemoi.backend.exception.FichierInvalideException;
import sn.parlemoi.backend.exception.RessourceNonTrouveeException;
import sn.parlemoi.backend.repository.ConversationRepository;
import sn.parlemoi.backend.repository.FichierRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import sn.parlemoi.backend.dto.fichier.FichierResponse;
import java.util.concurrent.TimeUnit;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FichierService {

    private static final long TAILLE_MAX_OCTETS = 10L * 1024 * 1024; // 10 Mo

    private final MinioClient minioClient;
    private final ConversationRepository conversationRepository;
    private final FichierRepository fichierRepository;
    private final String bucket;
    private final Tika tika = new Tika();

    public FichierService(
            MinioClient minioClient,
            ConversationRepository conversationRepository,
            FichierRepository fichierRepository,
            @Value("${minio.bucket}") String bucket
    ) {
        this.minioClient = minioClient;
        this.conversationRepository = conversationRepository;
        this.fichierRepository = fichierRepository;
        this.bucket = bucket;
    }

    // Version publique reutilisable par MessageService pour generer l'URL au moment
// de la diffusion WebSocket d'un nouveau message contenant un fichier, sans dupliquer
// la logique de signature deja ecrite pour le endpoint de telechargement.
    public String urlSigneePour(String cleObjet) {
        return genererUrlSignee(cleObjet);
    }

    @Transactional
    public FichierResponse obtenirAvecUrlSignee(String code, String fichierId) {
        Conversation conversation = conversationRepository.findByCode(code)
                .orElseThrow(() -> new RessourceNonTrouveeException("Conversation introuvable"));

        Fichier fichier = fichierRepository.findByIdAndConversationId(fichierId, conversation.getId())
                .orElseThrow(() -> new RessourceNonTrouveeException("Fichier introuvable pour cette conversation"));

        String urlSignee = genererUrlSignee(fichier.getCleObjet());

        return new FichierResponse(
                fichier.getId(), fichier.getNomOriginal(), fichier.getTypeMime(), fichier.getTailleOctets(), urlSignee
        );
    }

    private String genererUrlSignee(String cleObjet) {
        try {
            // URL valable 10 minutes seulement - jamais d'acces permanent a un fichier utilisateur.
            // Le client redemande une nouvelle URL a chaque fois qu'il veut re-telecharger apres expiration.
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(cleObjet)
                            .expiry(10, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de generer l'URL de telechargement", e);
        }
    }

    // Utilisee uniquement par le job de purge - jamais expose via un endpoint REST,
    // aucun utilisateur ne doit pouvoir declencher une suppression de fichier a la demande.
    public void supprimerObjet(String cleObjet) {
        try {
            minioClient.removeObject(
                    io.minio.RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(cleObjet)
                            .build()
            );
        } catch (Exception e) {
            // On ne bloque jamais la purge de la ligne DB pour un fichier deja absent du bucket
            // (ex: purge relancee apres un echec partiel precedent) - on logue et on continue.
            org.slf4j.LoggerFactory.getLogger(FichierService.class)
                    .warn("Impossible de supprimer l'objet MinIO {} pendant la purge", cleObjet, e);
        }
    }

    @Transactional
    public FichierUploadResponse uploader(String code, MultipartFile fichier) {
        Conversation conversation = conversationRepository.findByCode(code)
                .orElseThrow(() -> new RessourceNonTrouveeException("Conversation introuvable"));

        if (fichier.isEmpty()) {
            throw new FichierInvalideException("Le fichier est vide");
        }
        if (fichier.getSize() > TAILLE_MAX_OCTETS) {
            throw new FichierInvalideException("Le fichier depasse la taille maximale autorisee (10 Mo)");
        }

        // Detection du type MIME REEL par inspection du contenu binaire (Apache Tika),
        // jamais confiance dans le Content-Type declare par le client ou l'extension du nom -
        // les deux sont trivialement falsifiables par un attaquant.
        String typeMimeReel = detecterTypeMimeReel(fichier);
        if (!TypeFichierAutorise.AUTORISES.contains(typeMimeReel)) {
            throw new FichierInvalideException("Type de fichier non autorise : " + typeMimeReel);
        }

        String cleObjet = conversation.getId() + "/" + UUID.randomUUID() + extensionPour(typeMimeReel);

        try (InputStream flux = fichier.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(cleObjet)
                            .stream(flux, fichier.getSize(), -1)
                            .contentType(typeMimeReel)
                            .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors du stockage du fichier", e);
        }

        Fichier entite = Fichier.builder()
                .conversation(conversation)
                .nomOriginal(nettoyerNomFichier(fichier.getOriginalFilename()))
                .typeMime(typeMimeReel)
                .tailleOctets(fichier.getSize())
                .cleObjet(cleObjet)
                .build();

        Fichier sauvegarde = fichierRepository.save(entite);

        return new FichierUploadResponse(
                sauvegarde.getId(), sauvegarde.getNomOriginal(), sauvegarde.getTypeMime(), sauvegarde.getTailleOctets()
        );
    }

    private String detecterTypeMimeReel(MultipartFile fichier) {
        try (InputStream flux = fichier.getInputStream()) {
            return tika.detect(flux);
        } catch (IOException e) {
            throw new FichierInvalideException("Impossible de lire le fichier");
        }
    }

    private String extensionPour(String typeMime) {
        return switch (typeMime) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "video/mp4" -> ".mp4";
            case "video/webm" -> ".webm";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }

    // Empeche toute tentative de path traversal ou de caractere de controle dans le nom affiche -
    // le nom original n'est jamais utilise pour construire un chemin reel (cleObjet s'en charge avec un UUID)
    private String nettoyerNomFichier(String nomBrut) {
        if (nomBrut == null) {
            return "fichier";
        }
        return nomBrut.replaceAll("[\\\\/\\p{Cntrl}]", "_").trim();
    }
}