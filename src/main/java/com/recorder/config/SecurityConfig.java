package com.recorder.config;

import com.recorder.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import java.util.Arrays; // Importar Arrays

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final CustomUserDetailsService userDetailsService;
	private final JwtAuthFilter jwtAuthFilter;

	public SecurityConfig(CustomUserDetailsService userDetailsService, JwtAuthFilter jwtAuthFilter) {
		this.userDetailsService = userDetailsService;
		this.jwtAuthFilter = jwtAuthFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/actuator/health",
								"/api/auth/**",
								"/api/usuario/registrar",
								"/swagger-ui/**",
								"/v3/api-docs/**",
								"/api/galeria/**" // <<-- PERMITIR ACESSO PÚBLICO À GALERIA
						).permitAll()
						// Permissões específicas para admin/profissional
						.requestMatchers("/api/agendamentos2/pendentes", "/api/agendamentos2/confirmados",
								"/api/agendamentos2/confirmar/**", "/api/agendamentos2/recusar/**")
						.hasAnyAuthority("ROLE_PROFISSIONAL", "ROLE_ADMIN")
						.requestMatchers("/api/galeria/upload").hasAnyAuthority("ROLE_PROFISSIONAL", "ROLE_ADMIN") // Apenas
																													// profissionais
																													// podem
																													// fazer
																													// upload
						// Qualquer outra requisição precisa de autenticação
						.anyRequest().authenticated())
				.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("https://meu-frontend-tcc.onrender.com"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("Location"));
		config.setAllowCredentials(true); // This is the critical line
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

}
