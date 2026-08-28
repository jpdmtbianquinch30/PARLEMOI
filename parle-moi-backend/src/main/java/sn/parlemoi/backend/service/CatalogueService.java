package sn.parlemoi.backend.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sn.parlemoi.backend.dto.catalogue.FormuleResponse;
import sn.parlemoi.backend.dto.catalogue.ServiceResponse;
import sn.parlemoi.backend.entity.Formule;
import sn.parlemoi.backend.repository.FormuleRepository;
import sn.parlemoi.backend.repository.ServiceRepository;

import java.util.List;

@Service
public class CatalogueService {

    private final ServiceRepository serviceRepository;
    private final FormuleRepository formuleRepository;

    public CatalogueService(ServiceRepository serviceRepository, FormuleRepository formuleRepository) {
        this.serviceRepository = serviceRepository;
        this.formuleRepository = formuleRepository;
    }

    @Transactional
    public List<ServiceResponse> listerServicesActifs() {
        return serviceRepository.findByActifTrueOrderByOrdreAffichageAsc()
                .stream()
                .map(this::versServiceResponse)
                .toList();
    }

    private ServiceResponse versServiceResponse(sn.parlemoi.backend.entity.Service service) {
        List<FormuleResponse> formules = formuleRepository
                .findByServiceIdAndActifTrueOrderByOrdreAffichageAsc(service.getId())
                .stream()
                .map(this::versFormuleResponse)
                .toList();

        return new ServiceResponse(
                service.getId(),
                service.getNom(),
                service.getDescription(),
                formules
        );
    }

    private FormuleResponse versFormuleResponse(Formule formule) {
        return new FormuleResponse(
                formule.getId(),
                formule.getNom(),
                formule.getDescription(),
                formule.getDureeMinutes(),
                formule.getPrix(),
                formule.getDevise()
        );
    }
}