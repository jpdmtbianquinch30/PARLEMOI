package sn.parlemoi.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.parlemoi.backend.dto.admin.*;
import sn.parlemoi.backend.service.AdminCatalogueService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminCatalogueController {

    private final AdminCatalogueService adminCatalogueService;

    public AdminCatalogueController(AdminCatalogueService adminCatalogueService) {
        this.adminCatalogueService = adminCatalogueService;
    }

    @GetMapping("/catalogue")
    public List<ServiceAdminResponse> lister() {
        return adminCatalogueService.lister();
    }

    @PostMapping("/services/{serviceId}/formules")
    public ResponseEntity<FormuleAdminResponse> creerFormule(
            @PathVariable String serviceId,
            @Valid @RequestBody CreerFormuleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminCatalogueService.creerFormule(serviceId, request));
    }

    @PutMapping("/formules/{id}")
    public FormuleAdminResponse modifierFormule(
            @PathVariable String id,
            @Valid @RequestBody ModifierFormuleRequest request
    ) {
        return adminCatalogueService.modifierFormule(id, request);
    }
}