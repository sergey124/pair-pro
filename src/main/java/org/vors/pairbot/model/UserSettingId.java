package org.vors.pairbot.model;

import org.vors.pairbot.constant.SettingKey;
import java.io.Serializable;
import java.util.Objects;

public class UserSettingId implements Serializable {
    private Long user;
    private SettingKey setting;

    public UserSettingId() {
    }

    public UserSettingId(Long user, SettingKey setting) {
        this.user = user;
        this.setting = setting;
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.user, this.setting);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!UserSettingId.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final UserSettingId other = (UserSettingId) obj;

        return this.getUser().equals(other.getUser()) && this.getSetting().equals(other.getSetting());
    }

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public SettingKey getSetting() {
        return setting;
    }

    public void setSetting(SettingKey setting) {
        this.setting = setting;
    }
}