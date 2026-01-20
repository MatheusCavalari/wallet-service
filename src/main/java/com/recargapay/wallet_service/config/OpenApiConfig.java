package com.recargapay.wallet_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wallet Service API")
                        .version("0.1.0")
                        .description(
                                "APIs do wallet-service responsáveis por criar wallets, consultar saldo atual e histórico, " +
                                        "e executar operações de depósito, saque e transferência entre wallets. " +
                                        "O serviço mantém rastreabilidade completa via ledger de lançamentos, garantindo auditabilidade."
                        )
                        .contact(new Contact()
                                .name("Matheus Cavalari")
                                .email("matheuscavbarbosa@hotmail.com")
                        )
                );
    }
}
