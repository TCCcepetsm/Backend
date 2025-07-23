package com.recorder.controller.entity;

import com.recorder.controller.entity.enuns.TipoMidia;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "galeria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Galeria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "midia_url", nullable = false, length = 500)
    private String midiaUrl; // URL da foto/vídeo
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMidia tipo;  // Enum para Foto ou Vídeo
    
    @Column(name = "profissional_id")
    private Integer profissionalId; // ID do profissional
    
    @Column(name = "data_postagem")
    private LocalDateTime dataPostagem;
    
    @PrePersist
    protected void onCreate() {
        dataPostagem = LocalDateTime.now();
    }
}

