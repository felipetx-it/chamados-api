package com.empresa.chamados;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal da aplicação.
 *
 * A anotação @SpringBootApplication habilita a configuração automática do
 * Spring Boot, o escaneamento de componentes (@RestController etc.) e o
 * suporte a configurações externas (application.properties).
 */
@SpringBootApplication
public class ChamadosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChamadosApiApplication.class, args);
    }
}
