package com.recorder.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UsuarioResponse {
	private final Long idUsuario;
	private final String nome;
	private final String email;
	private final String telefone;
	private final String cpf;
	private final List<String> roles;

	public UsuarioResponse(Long idUsuario, String nome, String email,
			String telefone, String cpf, List<String> roles) {
		// Validação dos parâmetros
		Objects.requireNonNull(idUsuario, "ID do usuário não pode ser nulo");
		Objects.requireNonNull(nome, "Nome não pode ser nulo");
		Objects.requireNonNull(email, "Email não pode ser nulo");
		Objects.requireNonNull(telefone, "Telefone não pode ser nulo");
		Objects.requireNonNull(roles, "Roles não pode ser nulo");

		this.idUsuario = idUsuario;
		this.nome = nome;
		this.email = email;
		this.telefone = telefone;
		this.cpf = cpf; // CPF pode ser nulo para PJ

		// Normaliza as roles para garantir o prefixo ROLE_
		this.roles = roles.stream()
				.filter(Objects::nonNull)
				.map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
				.collect(Collectors.toUnmodifiableList());
	}

	// Getters (sem setters para imutabilidade)
	public Long getIdUsuario() {
		return idUsuario;
	}

	public String getNome() {
		return nome;
	}

	public String getEmail() {
		return email;
	}

	public String getTelefone() {
		return telefone;
	}

	public String getCpf() {
		return cpf;
	}

	public List<String> getRoles() {
		return roles; // Já é unmodifiable devido ao toUnmodifiableList()
	}

	// Métodos utilitários
	public boolean hasRole(String role) {
		String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
		return roles.contains(normalizedRole);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UsuarioResponse that = (UsuarioResponse) o;
		return Objects.equals(idUsuario, that.idUsuario) &&
				Objects.equals(email, that.email);
	}

	@Override
	public int hashCode() {
		return Objects.hash(idUsuario, email);
	}

	@Override
	public String toString() {
		return "UsuarioResponse{" +
				"idUsuario=" + idUsuario +
				", nome='" + nome + '\'' +
				", email='" + email + '\'' +
				", roles=" + roles +
				'}';
	}
}