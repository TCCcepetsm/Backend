package com.recorder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.recorder.repository") // Aponta para o pacote dos seus repositórios
@EnableTransactionManagement // Habilita o gerenciamento de transações do Spring
public class JpaConfig {
    // Esta classe não precisa de conteúdo.
    // As anotações no topo já fazem todo o trabalho de configurar o JPA
    // para encontrar seus repositórios e gerenciar transações corretamente.
}