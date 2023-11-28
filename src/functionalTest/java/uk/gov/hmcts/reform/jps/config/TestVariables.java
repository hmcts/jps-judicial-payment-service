package uk.gov.hmcts.reform.jps.config;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.jps.testutils.PropertiesReader;
import uk.gov.hmcts.reform.jps.testutils.RandomDateGenerator;

@SuppressWarnings("HideUtilityClassConstructor")
public class TestVariables {
    protected static final Logger LOGGER = LoggerFactory.getLogger(TestVariables.class);
    protected static final String testUrl;
    protected static final String recorderUsername;
    protected static final String recorderPassword;
    protected static final String submitterUsername;
    protected static final String submitterPassword;
    protected static final String publisherUsername;
    protected static final String publisherPassword;
    protected static final String adminUsername;
    protected static final String adminPassword;
    protected static final String johAdminUsername;
    protected static final String johAdminPassword;
    protected static final String invalidUsername;
    protected static final String invalidPassword;
    protected static String accessToken;
    protected static String recorderAccessToken;
    protected static String submitterAccessToken;
    protected static String publisherAccessToken;
    protected static String adminAccessToken;
    protected static String johAdminAccessToken;
    protected static String invalidAccessToken;
    protected static String validS2sToken;
    protected static String invalidS2sToken;
    protected static String judgeRoleTypeId = RandomStringUtils.randomAlphabetic(10);
    protected static String randomDate = RandomDateGenerator.generateRandomDate().toString();
    protected static String recordAttribute;
    protected static PropertiesReader propertiesReader = new PropertiesReader(
        "src/functionalTest/resources/test-config.properties");

    static {
        testUrl = propertiesReader.getProperty("test-url");
        recorderUsername = propertiesReader.getProperty("idam.recorder.username");
        recorderPassword = propertiesReader.getProperty("idam.recorder.password");
        submitterUsername = propertiesReader.getProperty("idam.submitter.username");
        submitterPassword = propertiesReader.getProperty("idam.submitter.password");
        publisherUsername = propertiesReader.getProperty("idam.publisher.username");
        publisherPassword = propertiesReader.getProperty("idam.publisher.password");
        adminUsername = propertiesReader.getProperty("idam.admin.username");
        adminPassword = propertiesReader.getProperty("idam.admin.password");
        johAdminUsername = propertiesReader.getProperty("idam.johAdmin.username");
        johAdminPassword = propertiesReader.getProperty("idam.johAdmin.password");
        invalidUsername = propertiesReader.getProperty("idam.invalid.username");
        invalidPassword = propertiesReader.getProperty("idam.invalid.password");
    }
}
