package sn.parlemoi.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import sn.parlemoi.backend.entity.Formule;
import sn.parlemoi.backend.repository.FormuleRepository;
import sn.parlemoi.backend.repository.ServiceRepository;

import java.math.BigDecimal;

@Component
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private final ServiceRepository serviceRepository;
    private final FormuleRepository formuleRepository;

    public DataSeeder(ServiceRepository serviceRepository, FormuleRepository formuleRepository) {
        this.serviceRepository = serviceRepository;
        this.formuleRepository = formuleRepository;
    }

    @Override
    public void run(String... args) {
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
                .service(service)
                .nom("Decouverte")
                .description("Ideal pour un premier echange")
                .dureeMinutes(30)
                .prix(new BigDecimal("10000.00"))
                .devise("XOF")
                .ordreAffichage(1)
                .actif(true)
                .build();

        Formule confort = Formule.builder()
                .service(service)
                .nom("Confort")
                .description("Pour approfondir la discussion")
                .dureeMinutes(60)
                .prix(new BigDecimal("15000.00"))
                .devise("XOF")
                .ordreAffichage(2)
                .actif(true)
                .build();

        Formule premium = Formule.builder()
                .service(service)
                .nom("Premium")
                .description("Pour un accompagnement approfondi et sans limite de temps ressentie")
                .dureeMinutes(120)
                .prix(new BigDecimal("25000.00"))
                .devise("XOF")
                .ordreAffichage(3)
                .actif(true)
                .build();

        formuleRepository.save(decouverte);
        formuleRepository.save(confort);
        formuleRepository.save(premium);

        System.out.println("=== DONNEES DE TEST INJECTEES ===");
        System.out.println("Service ID: " + service.getId());
        System.out.println("Decouverte ID: " + decouverte.getId());
        System.out.println("Confort ID: " + confort.getId());
        System.out.println("Premium ID: " + premium.getId());
        System.out.println("==================================");
    }
}