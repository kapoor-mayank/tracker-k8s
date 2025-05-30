package org.traccar.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;


public class Config {
    private final Properties properties = new Properties();

    private boolean useEnvironmentVariables;


    public Config() {
    }

    public Config(String file) throws IOException {
        try {
            Properties mainProperties = new Properties();
            try (InputStream inputStream = new FileInputStream(file)) {
                mainProperties.loadFromXML(inputStream);
            }

            String defaultConfigFile = mainProperties.getProperty("config.default");
            if (defaultConfigFile != null) {
                try (InputStream inputStream = new FileInputStream(defaultConfigFile)) {


                    this.properties.loadFromXML(inputStream);
                }
            }

            this.properties.putAll(mainProperties);

            this
                    .useEnvironmentVariables = (Boolean.parseBoolean(System.getenv("CONFIG_USE_ENVIRONMENT_VARIABLES")) || Boolean.parseBoolean(this.properties.getProperty("config.useEnvironmentVariables")));
        } catch (InvalidPropertiesFormatException e) {
            throw new RuntimeException("Configuration file is not a valid XML document", e);
        }
    }

    public boolean hasKey(ConfigKey key) {
        return hasKey(key.getKey());
    }

    @Deprecated
    public boolean hasKey(String key) {
        return ((this.useEnvironmentVariables && System.getenv().containsKey(getEnvironmentVariableName(key))) || this.properties
                .containsKey(key));
    }

    public String getString(ConfigKey key) {
        return getString(key.getKey());
    }

    @Deprecated
    public String getString(String key) {
        if (this.useEnvironmentVariables) {
            String value = System.getenv(getEnvironmentVariableName(key));
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return this.properties.getProperty(key);
    }

    public String getString(ConfigKey key, String defaultValue) {
        return getString(key.getKey(), defaultValue);
    }

    @Deprecated
    public String getString(String key, String defaultValue) {
        return hasKey(key) ? getString(key) : defaultValue;
    }

    public boolean getBoolean(ConfigKey key) {
        return getBoolean(key.getKey());
    }

    @Deprecated
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    public int getInteger(ConfigKey key) {
        return getInteger(key.getKey());
    }

    @Deprecated
    public int getInteger(String key) {
        return getInteger(key, 0);
    }

    public int getInteger(ConfigKey key, int defaultValue) {
        return getInteger(key.getKey(), defaultValue);
    }

    @Deprecated
    public int getInteger(String key, int defaultValue) {
        return hasKey(key) ? Integer.parseInt(getString(key)) : defaultValue;
    }

    public long getLong(ConfigKey key) {
        return getLong(key.getKey());
    }

    @Deprecated
    public long getLong(String key) {
        return getLong(key, 0L);
    }

    public long getLong(ConfigKey key, long defaultValue) {
        return getLong(key.getKey(), defaultValue);
    }

    @Deprecated
    public long getLong(String key, long defaultValue) {
        return hasKey(key) ? Long.parseLong(getString(key)) : defaultValue;
    }

    public double getDouble(ConfigKey key) {
        return getDouble(key.getKey());
    }

    @Deprecated
    public double getDouble(String key) {
        return getDouble(key, 0.0D);
    }

    public double getDouble(ConfigKey key, double defaultValue) {
        return getDouble(key.getKey(), defaultValue);
    }

    @Deprecated
    public double getDouble(String key, double defaultValue) {
        return hasKey(key) ? Double.parseDouble(getString(key)) : defaultValue;
    }

    public void setString(ConfigKey key, String value) {
        setString(key.getKey(), value);
    }

    @Deprecated
    public void setString(String key, String value) {
        this.properties.put(key, value);
    }

    static String getEnvironmentVariableName(String key) {
        return key.replaceAll("\\.", "_").replaceAll("(\\p{Lu})", "_$1").toUpperCase();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\config\Config.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */