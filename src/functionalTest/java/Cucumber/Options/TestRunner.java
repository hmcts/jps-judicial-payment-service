package Cucumber.Options;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/functionalTest/java/uk/gov/hmcts/reform/hmc/jp/functional/features",
//    plugin = {"pretty",
//        "html:target/cucumberReports/cucumber-report.html",
//        "json:target/cucumberReports/cucumber-report.json"
//    },
    glue = {"uk.gov.hmcts.reform.hmc.jp.functional.stepDefinitions"})
public class TestRunner {
}
