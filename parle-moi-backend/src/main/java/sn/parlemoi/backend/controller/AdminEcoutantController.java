package sn.parlemoi.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.parlemoi.backend.dto.admin.CreerEcoutantRequest;
import sn.parlemoi.backend.dto.admin.EcoutantAdminResponse;
import sn.parlemoi.backend.dto.admin.ModifierEcoutantRequest;
import sn.parlemoi.backend.service.AdminEcoutantService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ecoutants")
public class AdminEcoutantController {

    private final AdminEcoutantService adminEcoutantService;

    public AdminEcoutantController(AdminEcoutantService adminEcoutantService) {
        this.adminEcoutantService = adminEcoutantService;
    }

    @GetMapping
    public List<EcoutantAdminResponse> lister() {
        return adminEcoutantService.lister();
    }

    @PostMapping
    public ResponseEntity<EcoutantAdminResponse> creer(@Valid @RequestBody CreerEcoutantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminEcoutantService.creer(request));
    }

    @PutMapping("/{id}")
    public EcoutantAdminResponse modifier(
            @PathVariable String id,
            @Valid @RequestBody ModifierEcoutantRequest request
    ) {
        return adminEcoutantService.modifier(id, request);
    }
}