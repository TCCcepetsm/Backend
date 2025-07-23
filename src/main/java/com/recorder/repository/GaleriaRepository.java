package com.recorder.repository;

import com.recorder.controller.entity.Galeria;
import com.recorder.controller.entity.enuns.TipoMidia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GaleriaRepository extends JpaRepository<Galeria, Integer> {
    
    List<Galeria> findByTipo(TipoMidia tipo);
    
    List<Galeria> findByProfissionalId(Integer profissionalId);
    
    List<Galeria> findByTipoAndProfissionalId(TipoMidia tipo, Integer profissionalId);
}

