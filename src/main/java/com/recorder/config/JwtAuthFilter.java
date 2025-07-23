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
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String AUTHORIZATION_HEADER = "Authorization";

	private static final List<String> PUBLIC_ENDPOINTS = List.of(
			"/api/auth/",
			"/api/usuario/registrar",
			"/actuator/health",
			"/swagger-ui/",
			"/v3/api-docs/");

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

		final String requestPath = request.getServletPath();
		final String method = request.getMethod();

		try {
			// Handle OPTIONS requests first
			if ("OPTIONS".equalsIgnoreCase(method)) {
				response.setStatus(HttpServletResponse.SC_OK);
				return;
			}

			// Skip authentication for public endpoints
			if (isPublicEndpoint(requestPath)) {
				filterChain.doFilter(request, response);
				return;
			}

			// Extract and validate JWT
			final String jwt = extractJwtFromRequest(request);
			if (jwt == null) {
				throw new SecurityException("Missing or invalid Authorization header");
			}

			// Authenticate the token
			authenticateToken(jwt, request);

			// Continue the filter chain
			filterChain.doFilter(request, response);

		} catch (Exception e) {
			handleAuthenticationError(response, e);
		}
	}

	private String extractJwtFromRequest(HttpServletRequest request) {
		final String authHeader = request.getHeader(AUTHORIZATION_HEADER);
		if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
			return authHeader.substring(BEARER_PREFIX.length());
		}
		return null;
	}

	private boolean isPublicEndpoint(String requestPath) {
		return PUBLIC_ENDPOINTS.stream()
				.anyMatch(requestPath::startsWith);
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
			throw new SecurityException("Token does not contain a valid role");
		}

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
				userDetails,
				null,
				Collections.singletonList(new SimpleGrantedAuthority(role)));

		authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authToken);

		logger.debug("Authenticated user: {} with role: {}", userEmail, role);
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