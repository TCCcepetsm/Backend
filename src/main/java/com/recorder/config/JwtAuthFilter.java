package com.recorder.config;

import com.recorder.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
	private static final List<String> PUBLIC_ENDPOINTS = List.of(
			"/api/auth/",
			"/api/usuario/registrar",
			"/actuator/health");

	private final JwtService jwtService;
	private final CustomUserDetailsService userDetailsService;

	public JwtAuthFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain)
			throws ServletException, IOException {

		// Configura headers CORS
		response.setHeader("Access-Control-Allow-Origin", "https://meu-frontend-tcc.onrender.com");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Max-Age", "3600");

		// Para requisições OPTIONS, retorne imediatamente
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}
	}

	private void configureCorsHeaders(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "https://meu-frontend-tcc.onrender.com");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
		response.setHeader("Access-Control-Expose-Headers", "Authorization");
	}

	private boolean isPublicEndpoint(HttpServletRequest request) {
		return PUBLIC_ENDPOINTS.stream()
				.anyMatch(path -> request.getServletPath().startsWith(path));
	}

	private void authenticateToken(String jwt, HttpServletRequest request) {
		final String userEmail = jwtService.extractUsername(jwt);

		if (userEmail == null || SecurityContextHolder.getContext().getAuthentication() != null) {
			return;
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
		validateToken(jwt, userDetails);

		String role = jwtService.extractRole(jwt);
		if (role == null) {
			throw new SecurityException("Token without valid role");
		}

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
				userDetails,
				null,
				Collections.singletonList(new SimpleGrantedAuthority(role)));

		authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authToken);
		logger.debug("Authenticated user: {}", userEmail);
	}

	private void validateToken(String jwt, UserDetails userDetails) {
		if (userDetails == null) {
			throw new UsernameNotFoundException("User not found");
		}
		if (!jwtService.isTokenValid(jwt, userDetails)) {
			throw new SecurityException("Invalid token");
		}
	}

	private void handleAuthenticationError(HttpServletResponse response, Exception e) throws IOException {
		SecurityContextHolder.clearContext();
		logger.error("Authentication error: {}", e.getMessage());

		if (e instanceof UsernameNotFoundException) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
		} else if (e instanceof SecurityException) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
		} else {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
		}
	}
}