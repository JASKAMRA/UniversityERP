package edu.univ.erp.domain;

import java.util.Objects;

/**
 * Simple key-value application setting.
 */
public class Setting {
    private String key;
    private String value;

    public Setting() {}

    public Setting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    @Override
    public String toString() {
        return "Setting{" + "key='" + key + '\'' + ", value='" + value + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Setting)) return false;
        Setting s = (Setting) o;
        return Objects.equals(key, s.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
