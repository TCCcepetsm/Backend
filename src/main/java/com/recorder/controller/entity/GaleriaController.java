package com.recorder.controller.entity;

import com.recorder.controller.entity.enuns.TipoMidia;
import com.recorder.repository.GaleriaRepository;
import com.recorder.service.SupabaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/galeria")
@CrossOrigin(origins = "*")
public class GaleriaController {

    @Autowired
    private GaleriaRepository galeriaRepository;

    @Autowired
    private SupabaseStorageService supabaseStorageService;

    @GetMapping
    public ResponseEntity<List<Galeria>> listarTodas() {
        try {
            List<Galeria> galerias = galeriaRepository.findAll();
            return ResponseEntity.ok(galerias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Galeria> buscarPorId(@PathVariable Integer id) {
        try {
            Optional<Galeria> galeria = galeriaRepository.findById(id);
            return galeria.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Galeria> uploadMidia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tipo") String tipo,
            @RequestParam(value = "profissionalId", required = false) Integer profissionalId) {
        try {
            // Validar tipo de arquivo
            TipoMidia tipoMidia = TipoMidia.valueOf(tipo.toUpperCase());
            
            // Validar se o arquivo não está vazio
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Fazer upload para o Supabase
            String urlMidia = supabaseStorageService.uploadFile(file, "galeria");

            // Criar nova entrada na galeria
            Galeria galeria = new Galeria();
            galeria.setMidiaUrl(urlMidia);
            galeria.setTipo(tipoMidia);
            galeria.setProfissionalId(profissionalId);

            Galeria galeriaSalva = galeriaRepository.save(galeria);
            return ResponseEntity.status(HttpStatus.CREATED).body(galeriaSalva);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        try {
            Optional<Galeria> galeria = galeriaRepository.findById(id);
            if (galeria.isPresent()) {
                // Deletar arquivo do Supabase
                supabaseStorageService.deleteFile(galeria.get().getMidiaUrl());
                
                // Deletar registro do banco
                galeriaRepository.deleteById(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Galeria>> buscarPorTipo(@PathVariable String tipo) {
        try {
            TipoMidia tipoMidia = TipoMidia.valueOf(tipo.toUpperCase());
            List<Galeria> galerias = galeriaRepository.findByTipo(tipoMidia);
            return ResponseEntity.ok(galerias);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

