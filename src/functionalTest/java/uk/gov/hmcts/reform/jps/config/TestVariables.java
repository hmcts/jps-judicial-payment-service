package uk.gov.hmcts.reform.jps.config;

import org.apache.commons.lang3.RandomStringUtils;
import uk.gov.hmcts.reform.jps.testutils.PropertiesReader;

@SuppressWarnings("HideUtilityClassConstructor")
public class TestVariables {
    protected static final String testUrl;
    protected static final String recorderUsername;
    protected static final String recorderPassword;
    protected static final String submitterUsername;
    protected static final String submitterPassword;
    protected static final String publisherUsername;
    protected static final String publisherPassword;
    protected static final String adminUsername;
    protected static final String adminPassword;
    protected static final String invalidUsername;
    protected static final String invalidPassword;
    protected static String accessToken;
    protected static String recorderAccessToken;
    protected static String submitterAccessToken;
    protected static String publisherAccessToken;
    protected static String invalidAccessToken;
    protected static String validS2sToken;
    protected static String invalidS2sToken;
    protected static String judgeRoleTypeId = RandomStringUtils.randomAlphabetic(10);
    protected static String randomDate;

    static {
        PropertiesReader propertiesReader = new PropertiesReader("src/functionalTest/resources/test-config.properties");
        testUrl = propertiesReader.getProperty("test-url");
        recorderUsername = propertiesReader.getProperty("idam.recorder.username");
        recorderPassword = propertiesReader.getProperty("idam.recorder.password");
        submitterUsername = propertiesReader.getProperty("idam.submitter.username");
        submitterPassword = propertiesReader.getProperty("idam.submitter.password");
        publisherUsername = propertiesReader.getProperty("idam.publisher.username");
        publisherPassword = propertiesReader.getProperty("idam.publisher.password");
        adminUsername = propertiesReader.getProperty("idam.admin.username");
        adminPassword = propertiesReader.getProperty("idam.admin.password");
        invalidUsername = propertiesReader.getProperty("idam.invalid.username");
        invalidPassword = propertiesReader.getProperty("idam.invalid.password");
    }
}
