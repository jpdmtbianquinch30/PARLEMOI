package sn.parlemoi.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sn.parlemoi.backend.entity.Ecoutant;
import sn.parlemoi.backend.entity.Formule;
import sn.parlemoi.backend.enums.RoleEcoutant;
import sn.parlemoi.backend.repository.EcoutantRepository;
import sn.parlemoi.backend.repository.FormuleRepository;
import sn.parlemoi.backend.repository.ServiceRepository;

import java.math.BigDecimal;

@Component
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private final ServiceRepository serviceRepository;
    private final FormuleRepository formuleRepository;
    private final EcoutantRepository ecoutantRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            ServiceRepository serviceRepository,
            FormuleRepository formuleRepository,
            EcoutantRepository ecoutantRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.serviceRepository = serviceRepository;
        this.formuleRepository = formuleRepository;
        this.ecoutantRepository = ecoutantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedCatalogue();
        seedComptes();
    }

    private void seedCatalogue() {
        if (serviceRepository.count() > 0) {
            return;
        }

        sn.parlemoi.backend.entity.Service service = sn.parlemoi.backend.entity.Service.builder()
                .nom("Ecoute individuelle")
                .description("Une session d'ecoute confidentielle et bienveillante")
                .ordreAffichage(1)
                .actif(true)
                .build();
        service = serviceRepository.save(service);

        Formule decouverte = Formule.builder()
                .service(service).nom("Decouverte").description("Ideal pour un premier echange")
                .dureeMinutes(30).prix(new BigDecimal("10000.00")).devise("XOF")
                .ordreAffichage(1).actif(true).build();

        Formule confort = Formule.builder()
                .service(service).nom("Confort").description("Pour approfondir la discussion")
                .dureeMinutes(60).prix(new BigDecimal("15000.00")).devise("XOF")
                .ordreAffichage(2).actif(true).build();

        Formule premium = Formule.builder()
                .service(service).nom("Premium").description("Pour un accompagnement approfondi")
                .dureeMinutes(120).prix(new BigDecimal("25000.00")).devise("XOF")
                .ordreAffichage(3).actif(true).build();

        formuleRepository.save(decouverte);
        formuleRepository.save(confort);
        formuleRepository.save(premium);

        System.out.println("=== CATALOGUE INJECTE ===");
        System.out.println("Service ID: " + service.getId());
    }

    private void seedComptes() {
        if (ecoutantRepository.count() > 0) {
            return;
        }

        Ecoutant admin = Ecoutant.builder()
                .email("admin@parlemoi.sn")
                .motDePasseHash(passwordEncoder.encode("ChangeMoi123!"))
                .nom("Administrateur")
                .role(RoleEcoutant.ADMIN)
                .enLigne(false)
                .build();

        Ecoutant ecoutante = Ecoutant.builder()
                .email("ecoutante@parlemoi.sn")
                .motDePasseHash(passwordEncoder.encode("ChangeMoi123!"))
                .nom("Ecoutante Test")
                .role(RoleEcoutant.ECOUTANT)
                .enLigne(false)
                .build();

        ecoutantRepository.save(admin);
        ecoutantRepository.save(ecoutante);

        System.out.println("=== COMPTES DE TEST INJECTES (mot de passe: ChangeMoi123!) ===");
        System.out.println("Admin    : admin@parlemoi.sn");
        System.out.println("Ecoutant : ecoutante@parlemoi.sn");
    }
}