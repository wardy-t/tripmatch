package com.tomward.tripmatch.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration {

    @Bean
    fun tripMatchOpenApi(): OpenAPI =
        OpenAPI().info(
            Info()
                .title("TripMatch API")
                .description(
                    "A travel destination recommendation API built " +
                        "with Kotlin and Spring Boot."
                )
                .version("1.0.0")
                .contact(
                    Contact().name("Tom Ward")
                )
        )
}