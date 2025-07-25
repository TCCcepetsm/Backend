package com.recorder.controller.entity;

import com.recorder.dto.AuthenticationRequest;

import com.recorder.dto.AuthenticationResponse;
import com.recorder.config.JwtService;
import com.recorder.controller.entity.Usuario;
import com.recorder.controller.entity.enuns.Roles;
import com.recorder.repository.AgendamentoRepository;
import com.recorder.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UsuarioRepository usuarioRepository;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha()));

		Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

		List<GrantedAuthority> authorities = usuario.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.name()))
				.collect(Collectors.toList());

		String jwtToken = jwtService.generateToken(new User(usuario.getEmail(), usuario.getSenha(), authorities));

		// --- PREPARAÇÃO DA RESPOSTA CORRIGIDA ---
		return ResponseEntity.ok(AuthenticationResponse.builder()
				.id(usuario.getIdUsuario()) // <<-- ADICIONE O ID AQUI
				.token(jwtToken)
				.email(usuario.getEmail())
				.nome(usuario.getNome())
				.roles(authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
				.build());
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logoutUser(HttpServletRequest request) {
		// Aqui você pode invalidar o token se estiver usando blacklist
		return ResponseEntity.ok().body("Logout realizado com sucesso");
	}

	@GetMapping("/validate-token")
	public ResponseEntity<?> validateToken(HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {
		try {
			String token = request.getHeader("Authorization").substring(7); // Remove "Bearer "
			if (jwtService.isTokenValid(token, userDetails)) { // Usando SEU método
				return ResponseEntity.ok().build();
			}
		} catch (Exception e) {
			// Token inválido ou ausente
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	@GetMapping("/after-login")
	public String afterLogin() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth != null && auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_PROFISSIONAL"))) {
			return "redirect:/inicialAdmin"; // Redireciona para a página do Admin/Profissional
		} else {
			return "redirect:/inicial"; // Redireciona para a página padrão (ex: home.html)
		}
	}
}