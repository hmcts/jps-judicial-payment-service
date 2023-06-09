package uk.gov.hmcts.reform.jps;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@OpenAPIDefinition
@EnableTransactionManagement
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.jps.refdata.location.client",
    "uk.gov.hmcts.reform.jps.refdata.judicial.client",
    "uk.gov.hmcts.reform.jps.refdata.caseworker.client"
})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
