package com.recorder.component;

import com.recorder.controller.entity.Agendamento;
import com.recorder.controller.entity.Usuario;
import com.recorder.controller.entity.enuns.StatusAgendamento;
import com.recorder.dto.AgendamentoDTO;
import org.springframework.stereotype.Component;

// Em component/AgendamentoMapper.java
@Component
public class AgendamentoMapper {

    public Agendamento toEntity(AgendamentoDTO dto, Usuario usuario) {
        Agendamento agendamento = new Agendamento();

        // Mapeamento correto: do DTO para a Entidade
        agendamento.setUsuario(usuario);
        agendamento.setNome(dto.getNome()); // <<-- CORREÇÃO
        agendamento.setEmail(dto.getEmail()); // <<-- CORREÇÃO
        agendamento.setTelefone(dto.getTelefone()); // <<-- CORREÇÃO
        agendamento.setPlano(dto.getPlano()); // <<-- CORREÇÃO
        agendamento.setData(dto.getData()); // <<-- CORREÇÃO
        agendamento.setHorario(dto.getHorario()); // <<-- CORREÇÃO
        agendamento.setEsporte(dto.getEsporte()); // <<-- CORREÇÃO
        agendamento.setLocal(dto.getLocal()); // <<-- CORREÇÃO
        agendamento.setLatitude(dto.getLatitude()); // <<-- CORREÇÃO
        agendamento.setLongitude(dto.getLongitude()); // <<-- CORREÇÃO

        // O status deve ser sempre PENDENTE na criação
        agendamento.setStatus(StatusAgendamento.PENDENTE);

        return agendamento;
    }
}
