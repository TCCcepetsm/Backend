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
    private Integer id; // O ID da própria galeria pode ser Integer, sem problemas.
    
    @Column(name = "midia_url", nullable = false, length = 500)
    private String midiaUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMidia tipo;
    
    // CORREÇÃO: Mudar de Integer para Long para ser compatível com o ID do Usuário
    @Column(name = "profissional_id")
    private Long profissionalId; // <<-- TIPO CORRIGIDO
    
    @Column(name = "data_postagem")
    private LocalDateTime dataPostagem;
    
    @PrePersist
    protected void onCreate() {
        dataPostagem = LocalDateTime.now();
    }
}