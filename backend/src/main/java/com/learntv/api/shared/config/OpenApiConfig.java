package com.learntv.api.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI learntvOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LearnTV API")
                        .description("English learning platform using TV shows as source material")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LearnTV Team")
                                .email("support@learntv.app"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development"),
                        new Server().url("https://api.learntv.app").description("Production")
                ));
    }
}
