package com.internal.projectmgmt.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI dashboardFinanceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dashboard Finance API")
                        .description("Internal Project Management System — API Documentation")
                        .version("1.0.0"));
    }
}
