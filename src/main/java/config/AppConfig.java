package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppConfig {
    private final Properties properties;

    public AppConfig() {
        properties = new Properties();
        loadProperties();
    }

    private void loadProperties() {
        try {
            File configFile = new File("src/config/config.properties");
            FileInputStream input = new FileInputStream(configFile);
            properties.load(input);
            System.out.println("✅ Конфигурация загружена");
        } catch (IOException e) {
            setDefaultProperties();
            System.out.println("⚠️ Используются значения по умолчанию");
        }
    }

    private void setDefaultProperties() {
        properties.setProperty("server.port", "8080");
        properties.setProperty("default.click.limit", "100");
        properties.setProperty("default.ttl.hours", "24");
        properties.setProperty("cleanup.interval.hours", "1");
        properties.setProperty("cleanup.initial.delay.minutes", "1");
        properties.setProperty("short.code.length", "6");
    }

    public int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port"));
    }

    public int getDefaultClickLimit() {
        return Integer.parseInt(properties.getProperty("default.click.limit"));
    }

    public int getDefaultTtlHours() {
        return Integer.parseInt(properties.getProperty("default.ttl.hours"));
    }

    public int getCleanupIntervalHours() {
        return Integer.parseInt(properties.getProperty("cleanup.interval.hours"));
    }

    public int getCleanupInitialDelayMinutes() {
        return Integer.parseInt(properties.getProperty("cleanup.initial.delay.minutes"));
    }

    public int getShortCodeLength() {
        return Integer.parseInt(properties.getProperty("short.code.length"));
    }
}