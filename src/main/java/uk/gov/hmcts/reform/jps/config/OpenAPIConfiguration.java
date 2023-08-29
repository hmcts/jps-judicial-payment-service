package uk.gov.hmcts.reform.jps.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .components(new Components()
                            .addSecuritySchemes("bearerAuth",
                                                new SecurityScheme()
                                                    .name("bearerAuth")
                                                    .type(SecurityScheme.Type.HTTP)
                                                    .scheme("bearer")
                                                    .bearerFormat("JWT"))
                            .addSecuritySchemes("serviceAuthorization",
                                                new SecurityScheme()
                                                    .in(SecurityScheme.In.HEADER)
                                                    .name("ServiceAuthorization")
                                                    .type(SecurityScheme.Type.APIKEY)
                                                    .scheme("bearer")
                                                    .bearerFormat("JWT")
                            )
            )
            .info(new Info().title("jps judicial payment service")
                      .description("jps judicial payment service")
                      .version("v0.0.1")
                      .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
            .externalDocs(new ExternalDocumentation()
                              .description("README")
                              .url("https://github.com/hmcts/jps-judicial-payment-service#readme"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .addSecurityItem(new SecurityRequirement().addList("serviceAuthorization"));
    }
}
