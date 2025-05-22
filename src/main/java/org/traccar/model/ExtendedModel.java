package org.traccar.model;

import java.util.LinkedHashMap;
import java.util.Map;


public class ExtendedModel
        extends BaseModel {
    private Map<String, Object> attributes = new LinkedHashMap<>();

    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public String toString() {
        return "ExtendedModel{" +
                "attributes=" + attributes +
                '}';
    }


    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void set(String key, Boolean value) {
        if (value != null) {
            this.attributes.put(key, value);
        }
    }

    public void set(String key, Byte value) {
        if (value != null) {
            this.attributes.put(key, Integer.valueOf(value.intValue()));
        }
    }

    public void set(String key, Short value) {
        if (value != null) {
            this.attributes.put(key, Integer.valueOf(value.intValue()));
        }
    }

    public void set(String key, Integer value) {
        if (value != null) {
            this.attributes.put(key, value);
        }
    }

    public void set(String key, Long value) {
        if (value != null) {
            this.attributes.put(key, value);
        }
    }

    public void set(String key, Float value) {
        if (value != null) {
            this.attributes.put(key, Double.valueOf(value.doubleValue()));
        }
    }

    public void set(String key, Double value) {
        if (value != null) {
            this.attributes.put(key, value);
        }
    }

    public void set(String key, String value) {
        if (value != null && !value.isEmpty()) {
            this.attributes.put(key, value);
        }
    }

    public void add(Map.Entry<String, Object> entry) {
        if (entry != null && entry.getValue() != null) {
            this.attributes.put(entry.getKey(), entry.getValue());
        }
    }

    public String getString(String key) {
        if (this.attributes.containsKey(key)) {
            return (String) this.attributes.get(key);
        }
        return null;
    }


    public double getDouble(String key) {
        if (this.attributes.containsKey(key)) {
            return ((Number) this.attributes.get(key)).doubleValue();
        }
        return 0.0D;
    }


    public boolean getBoolean(String key) {
        if (this.attributes.containsKey(key)) {
            return ((Boolean) this.attributes.get(key)).booleanValue();
        }
        return false;
    }


    public int getInteger(String key) {
        if (this.attributes.containsKey(key)) {
            return ((Number) this.attributes.get(key)).intValue();
        }
        return 0;
    }


    public long getLong(String key) {
        if (this.attributes.containsKey(key)) {
            return ((Number) this.attributes.get(key)).longValue();
        }
        return 0L;
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\model\ExtendedModel.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */