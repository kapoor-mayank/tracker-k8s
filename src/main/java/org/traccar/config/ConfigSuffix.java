package org.traccar.config;


public class ConfigSuffix
        extends ConfigKey {
    ConfigSuffix(String key, Class clazz) {
        super(key, clazz);
    }

    public ConfigKey withPrefix(String prefix) {
        return new ConfigKey(prefix + getKey(), getValueClass());
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\config\ConfigSuffix.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */