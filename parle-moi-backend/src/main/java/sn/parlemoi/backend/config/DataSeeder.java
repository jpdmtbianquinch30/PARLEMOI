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
            return; // deja peuple, on ne duplique jamais
        }

        sn.parlemoi.backend.entity.Service service = sn.parlemoi.backend.entity.Service.builder()
                .nom("Ecoute individuelle")
                .description("Une session d'ecoute confidentielle et bienveillante")
                .ordreAffichage(1)
                .actif(true)
                .build();
        service = serviceRepository.save(service);

        Formule formule30 = Formule.builder()
                .service(service)
                .nom("Session 30 minutes")
                .description("Ideal pour un premier echange")
                .dureeMinutes(30)
                .prix(new BigDecimal("5000.00"))
                .devise("XOF")
                .ordreAffichage(1)
                .actif(true)
                .build();

        Formule formule60 = Formule.builder()
                .service(service)
                .nom("Session 60 minutes")
                .description("Pour approfondir la discussion")
                .dureeMinutes(60)
                .prix(new BigDecimal("9000.00"))
                .devise("XOF")
                .ordreAffichage(2)
                .actif(true)
                .build();

        formuleRepository.save(formule30);
        formuleRepository.save(formule60);

        System.out.println("=== DONNEES DE TEST INJECTEES ===");
        System.out.println("Service ID: " + service.getId());
        System.out.println("Formule 30min ID: " + formule30.getId());
        System.out.println("Formule 60min ID: " + formule60.getId());
        System.out.println("==================================");
    }
}