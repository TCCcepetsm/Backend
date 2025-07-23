package com.recorder.controller.entity;

import com.recorder.controller.entity.enuns.Roles;
import com.recorder.repository.UsuarioRepository;
import com.recorder.dto.LoginDTO;
import com.recorder.dto.UsuarioDTO;
import com.recorder.dto.UsuarioResponse;
import com.recorder.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.recorder.dto.RegistroDTO;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

	private final UsuarioService usuarioService;
	private final UsuarioRepository usuarioRepository; // Adicionado

	// Adicione a injeção do repositório
	@Autowired
	public UsuarioController(UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
		this.usuarioService = usuarioService;
		this.usuarioRepository = usuarioRepository;
	}

	@PostMapping("/registrar")
	public ResponseEntity<?> registrar(@Valid @RequestBody RegistroDTO registroDTO, BindingResult result) {
		try {
			// Validação dos campos
			if (result.hasErrors()) {
				Map<String, String> errors = result.getFieldErrors().stream()
						.collect(Collectors.toMap(
								FieldError::getField,
								FieldError::getDefaultMessage));
				return ResponseEntity.badRequest().body(errors);
			}

			// Verificação adicional de senhas
			if (!registroDTO.getSenha().equals(registroDTO.getConfirmarSenha())) {
				return ResponseEntity.badRequest().body(
						Collections.singletonMap("error", "As senhas não coincidem"));
			}

			// Verificar se email já existe
			if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
				return ResponseEntity.badRequest().body(
						Collections.singletonMap("error", "Email já cadastrado"));
			}

			// Registrar usuário
			Usuario usuarioSalvo = usuarioService.registrar(registroDTO);

			// Retornar resposta
			return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
					"success", true,
					"message", "Usuário registrado com sucesso",
					"data", new UsuarioResponse(
							usuarioSalvo.getIdUsuario(),
							usuarioSalvo.getNome(),
							usuarioSalvo.getEmail(),
							usuarioSalvo.getTelefone(),
							usuarioSalvo.getCpf(),
							usuarioSalvo.getRoles().stream()
									.map(Roles::name)
									.collect(Collectors.toList()))));

		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(
					Collections.singletonMap("error", e.getMessage()));
		}
	}

	@GetMapping("/check-email")
	public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
		boolean exists = usuarioRepository.existsByEmail(email);
		return ResponseEntity.ok(exists);
	}

	private Map<String, Object> createUsuarioResponse(Usuario usuario) {
		return Map.of(
				"idUsuario", usuario.getIdUsuario(),
				"nome", usuario.getNome(),
				"email", usuario.getEmail(),
				"telefone", usuario.getTelefone(),
				"cpf", usuario.getCpf(),
				"roles", usuario.getRoles().stream()
						.map(Enum::name)
						.collect(Collectors.toList()));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
		try {
			Usuario usuario = usuarioService.autenticar(loginDTO.getEmail(), loginDTO.getSenha());
			return ResponseEntity.ok(usuario);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.status(401).body("Credenciais inválidas");
		}
	}

	// ======== CRUD AQUI ========

	@GetMapping
	public ResponseEntity<List<Usuario>> listarTodos() {
		return ResponseEntity.ok(usuarioService.listarTodos());
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
		try {
			Usuario usuario = usuarioService.buscarPorId(id);
			return ResponseEntity.ok(usuario);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody UsuarioDTO usuarioDTO) {
		try {
			Usuario atualizado = usuarioService.atualizar(id, usuarioDTO);
			return ResponseEntity.ok(atualizado);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deletar(@PathVariable Long id) {
		try {
			usuarioService.deletar(id);
			return ResponseEntity.ok().build();
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}
}
