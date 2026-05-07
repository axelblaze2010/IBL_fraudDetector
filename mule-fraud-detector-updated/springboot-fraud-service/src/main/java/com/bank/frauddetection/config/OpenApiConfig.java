package com.bank.frauddetection.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI muleFraudDetectorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Mule Account Network Detector API")
                        .description("Fraud detection APIs for mule account risk scoring, graph analysis, fraud cases, and false-positive reporting.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Fraud Detection Team")
                                .email("fraud-support@bank.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Server")
                ));
    }
}
