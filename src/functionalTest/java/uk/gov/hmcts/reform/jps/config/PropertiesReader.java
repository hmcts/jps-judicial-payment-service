package uk.gov.hmcts.reform.jps.config;

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
            int startIndex = value.indexOf("${");
            while (startIndex != -1) {
                int endIndex = findMatchingClosingBrace(value, startIndex + 2);
                if (endIndex != -1) {
                    String variableExpression = value.substring(startIndex + 2, endIndex);
                    String envVarName;
                    String defaultValue = null;

                    int colonIndex = variableExpression.indexOf(':');
                    if (colonIndex != -1) {
                        envVarName = variableExpression.substring(0, colonIndex);
                        defaultValue = variableExpression.substring(colonIndex + 1);
                    } else {
                        envVarName = variableExpression;
                    }

                    String environmentValue = System.getenv(envVarName);
                    if (environmentValue != null) {
                        value = value.substring(0, startIndex) + environmentValue + value.substring(endIndex + 1);
                    } else if (defaultValue != null) {
                        value = value.substring(0, startIndex) + defaultValue + value.substring(endIndex + 1);
                    } else {
                        System.err.println("Environment variable not found: " + envVarName);
                        break;
                    }
                } else {
                    break;
                }
                startIndex = value.indexOf("${", startIndex + 1);
            }
            properties.setProperty(key, value);
        }
    }

    private int findMatchingClosingBrace(String str, int startIndex) {
        int openBraces = 1;
        for (int i = startIndex; i < str.length(); i++) {
            if (str.charAt(i) == '{') {
                openBraces++;
            } else if (str.charAt(i) == '}') {
                openBraces--;
                if (openBraces == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}
