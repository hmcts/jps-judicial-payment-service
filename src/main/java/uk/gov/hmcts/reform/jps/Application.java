package uk.gov.hmcts.reform.jps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
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
