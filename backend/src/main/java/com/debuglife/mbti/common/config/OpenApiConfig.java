package com.debuglife.mbti.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI mbtiOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("/").description("Current deployment"))
                .info(new Info()
                        .title("MBTI AI Assessment API")
                        .version("v1")
                        .description("Modern API foundation for the MBTI AI Assessment Platform.")
                        .license(new License().name("MIT")));
    }
}
