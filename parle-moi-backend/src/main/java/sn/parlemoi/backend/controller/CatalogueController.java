package sn.parlemoi.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sn.parlemoi.backend.dto.catalogue.ServiceResponse;
import sn.parlemoi.backend.service.CatalogueService;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class CatalogueController {

    private final CatalogueService catalogueService;

    public CatalogueController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
    }

    @GetMapping
    public List<ServiceResponse> listerServices() {
        return catalogueService.listerServicesActifs();
    }
}