package sn.parlemoi.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sn.parlemoi.backend.dto.fichier.FichierUploadResponse;
import sn.parlemoi.backend.service.FichierService;

@RestController
@RequestMapping("/api/conversations")
public class FichierController {

    private final FichierService fichierService;

    public FichierController(FichierService fichierService) {
        this.fichierService = fichierService;
    }

    @PostMapping("/{code}/fichiers")
    public ResponseEntity<FichierUploadResponse> uploader(
            @PathVariable String code,
            @RequestParam("fichier") MultipartFile fichier
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fichierService.uploader(code, fichier));
    }
}