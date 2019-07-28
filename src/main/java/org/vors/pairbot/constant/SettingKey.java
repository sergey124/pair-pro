package org.vors.pairbot.constant;

public enum SettingKey {
    ;

    private SettingType type;
    private String defaultValue;

    SettingKey(SettingType type, String defaultValue) {
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public SettingType getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
