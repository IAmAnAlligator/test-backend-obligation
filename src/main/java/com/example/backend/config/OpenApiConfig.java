package com.example.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {

    return new OpenAPI()
        .info(
            new Info()
                .title("Тестовое задание: Backend-разработчик")
                .version("1.0")
                .description(
                    "Платформа «Умный реестр подписок» (Backend-ядро и AI-интеграция) "
                        + "для автоматизации учёта, "
                        + "расчёта дат списаний и контроля статуса подписок пользователя."));
  }
}
