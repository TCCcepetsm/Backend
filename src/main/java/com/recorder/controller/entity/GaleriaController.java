package com.recorder.controller.entity;

import com.recorder.controller.entity.enuns.TipoMidia;
import com.recorder.repository.GaleriaRepository;
import com.recorder.service.SupabaseStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
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

    // Endpoint para upload de múltiplos arquivos
    @PostMapping("/upload-multiple")
    public ResponseEntity<List<Galeria>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "profissionalId", required = false) Integer profissionalId) {

        if (files.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Galeria> savedGalerias = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // Determina o tipo de mídia pelo Content-Type
                TipoMidia tipoMidia = file.getContentType().startsWith("image/") ? TipoMidia.FOTO : TipoMidia.VIDEO;

                // Faz o upload para o Supabase
                String urlMidia = supabaseStorageService.uploadFile(file, "galeria");

                // Cria a entidade
                Galeria galeria = new Galeria();
                galeria.setMidiaUrl(urlMidia);
                galeria.setTipo(tipoMidia);
                galeria.setProfissionalId(profissionalId);

                savedGalerias.add(galeriaRepository.save(galeria));

            } catch (Exception e) {
                // Logar o erro pode ser útil aqui
                System.err
                        .println("Erro ao processar o arquivo: " + file.getOriginalFilename() + " - " + e.getMessage());
                // Pode-se optar por continuar ou retornar um erro
            }
        }

        if (savedGalerias.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(savedGalerias);
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
