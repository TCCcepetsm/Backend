package com.recorder.service;

import com.recorder.controller.entity.Usuario;
import com.recorder.controller.entity.enuns.Roles;
import com.recorder.dto.UsuarioDTO;
import com.recorder.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrar(UsuarioDTO usuarioDTO) {
        // Verificar se email já existe
        if (usuarioRepository.findByEmail(usuarioDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email já cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDTO.getNome());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setTelefone(usuarioDTO.getTelefone());
        usuario.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));

        // Definir roles baseadas no tipo
        Set<Roles> roles = new HashSet<>();
        if ("PF".equalsIgnoreCase(usuarioDTO.getTipo())) {
            usuario.setCpf(usuarioDTO.getCpf());
            roles.add(Roles.ROLE_USUARIO);
        } else if ("PJ".equalsIgnoreCase(usuarioDTO.getTipo())) {
            usuario.setCnpj(usuarioDTO.getCnpj());
            roles.add(Roles.ROLE_PROFISSIONAL);
        }
        usuario.setRoles(roles);

        return usuarioRepository.save(usuario);
    }

    // ========== AUTENTICAÇÃO ==========
    public Usuario autenticar(String email, String senha) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new IllegalArgumentException("Senha incorreta");
        }

        return usuario;
    }

    // ========== CRUD ==========
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @Transactional
    public Usuario atualizar(Long id, UsuarioDTO dto) {
        Usuario usuarioExistente = buscarPorId(id);

        // Atualiza campos básicos
        usuarioExistente.setNome(dto.getNome());
        usuarioExistente.setEmail(dto.getEmail());
        usuarioExistente.setTelefone(dto.getTelefone());

        // Atualiza senha se fornecida
        if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
            usuarioExistente.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        // Atualiza CPF/CNPJ e roles se necessário
        if (dto.getCpf() != null && !dto.getCpf().equals(usuarioExistente.getCpf())) {
            if (usuarioRepository.existsByCpf(dto.getCpf())) {
                throw new RuntimeException("CPF já cadastrado por outro usuário");
            }
            usuarioExistente.setCpf(dto.getCpf());
        }

        if (dto.getCnpj() != null && !dto.getCnpj().equals(usuarioExistente.getCnpj())) {
            if (usuarioRepository.existsByCnpj(dto.getCnpj())) {
                throw new RuntimeException("CNPJ já cadastrado por outro usuário");
            }
            usuarioExistente.setCnpj(dto.getCnpj());

            // Atualiza roles se CNPJ foi adicionado
            Set<Roles> roles = usuarioExistente.getRoles();
            roles.add(Roles.ROLE_PROFISSIONAL);
            usuarioExistente.setRoles(roles);
        }

        return usuarioRepository.save(usuarioExistente);
    }

    @Transactional
    public void deletar(Long id) {
        Usuario usuario = buscarPorId(id);
        usuarioRepository.delete(usuario);
    }
}