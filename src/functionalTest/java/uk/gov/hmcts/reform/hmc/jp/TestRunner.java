package uk.gov.hmcts.reform.hmc.jp;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/functionalTest/resources/features",
    plugin = {"pretty",
        "html:target/cucumberReports/cucumber-report.html",
        "json:target/cucumberReports/cucumber-report.json"
    },
    glue = {"uk.gov.hmcts.reform.hmc.jp"})
public class TestRunner {
}
