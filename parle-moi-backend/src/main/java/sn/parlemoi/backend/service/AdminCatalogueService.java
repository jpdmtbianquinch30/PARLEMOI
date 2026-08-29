package sn.parlemoi.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sn.parlemoi.backend.dto.admin.*;
import sn.parlemoi.backend.entity.Formule;
import sn.parlemoi.backend.exception.RessourceNonTrouveeException;
import sn.parlemoi.backend.repository.FormuleRepository;
import sn.parlemoi.backend.repository.ServiceRepository;

import java.util.List;

@Service
public class AdminCatalogueService {

    private final ServiceRepository serviceRepository;
    private final FormuleRepository formuleRepository;

    public AdminCatalogueService(ServiceRepository serviceRepository, FormuleRepository formuleRepository) {
        this.serviceRepository = serviceRepository;
        this.formuleRepository = formuleRepository;
    }

    @Transactional
    public List<ServiceAdminResponse> lister() {
        return serviceRepository.findAllByOrderByOrdreAffichageAsc()
                .stream()
                .map(this::versServiceReponse)
                .toList();
    }

    @Transactional
    public FormuleAdminResponse creerFormule(String serviceId, CreerFormuleRequest request) {
        sn.parlemoi.backend.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RessourceNonTrouveeException("Service introuvable"));

        Formule formule = Formule.builder()
                .service(service)
                .nom(request.nom())
                .description(request.description())
                .dureeMinutes(request.dureeMinutes())
                .prix(request.prix())
                .devise("XOF")
                .actif(true)
                .ordreAffichage(0)
                .build();

        return versFormuleReponse(formuleRepository.save(formule));
    }

    @Transactional
    public FormuleAdminResponse modifierFormule(String id, ModifierFormuleRequest request) {
        Formule formule = formuleRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Formule introuvable"));

        formule.setNom(request.nom());
        formule.setDescription(request.description());
        formule.setDureeMinutes(request.dureeMinutes());
        formule.setPrix(request.prix());
        formule.setActif(request.actif());

        return versFormuleReponse(formuleRepository.save(formule));
    }

    private ServiceAdminResponse versServiceReponse(sn.parlemoi.backend.entity.Service service) {
        List<FormuleAdminResponse> formules = formuleRepository
                .findByServiceIdOrderByOrdreAffichageAsc(service.getId())
                .stream()
                .map(this::versFormuleReponse)
                .toList();

        return new ServiceAdminResponse(
                service.getId(), service.getNom(), service.getDescription(), service.isActif(), formules
        );
    }

    private FormuleAdminResponse versFormuleReponse(Formule f) {
        return new FormuleAdminResponse(
                f.getId(), f.getNom(), f.getDescription(), f.getDureeMinutes(), f.getPrix(), f.getDevise(), f.isActif()
        );
    }
}