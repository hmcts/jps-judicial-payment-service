package uk.gov.hmcts.reform.jps.testutils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesReader {
    private final Properties properties;

    public PropertiesReader(String filePath) {
        properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            properties.load(fileInputStream);
            validateEnvironmentVariables();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    private void validateEnvironmentVariables() {
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (value.startsWith("${") && value.endsWith("}")) {
                String environmentVariable = value.substring(2, value.length() - 1);
                int colonIndex = environmentVariable.indexOf(':');
                String envVarName = colonIndex != -1 ? environmentVariable.substring(0, colonIndex) :
                    environmentVariable;
                String defaultValue = colonIndex != -1 ? environmentVariable.substring(colonIndex + 1) : null;

                String environmentValue = System.getenv(envVarName);
                if (environmentValue != null) {
                    properties.setProperty(key, environmentValue);
                } else if (defaultValue != null) {
                    properties.setProperty(key, defaultValue);
                } else {
                    System.err.println("Environment variable not found: " + envVarName);
                }
            }
        }
    }
}
