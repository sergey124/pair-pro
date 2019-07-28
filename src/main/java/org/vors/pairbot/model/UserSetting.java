package org.vors.pairbot.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.vors.pairbot.constant.SettingKey;

import javax.persistence.*;

@Entity
@Table(name = "user_settings")
@EntityListeners(AuditingEntityListener.class)
@IdClass(UserSettingId.class)
public class UserSetting {
    @Id
    @ManyToOne
    @JoinColumn(name = "user_pk", referencedColumnName = "pk", nullable = false)
    private UserInfo user;

    @Id
    @Enumerated
    @Column(columnDefinition = "smallint")
    private SettingKey setting;
    @Column
    private String value;

    public UserSetting() {
    }

    public UserSetting(UserInfo user, SettingKey setting, String value) {
        this.user = user;
        this.setting = setting;
        this.value = value;
    }

    public UserSetting(UserInfo user, SettingKey setting) {
        this.user = user;
        this.setting = setting;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public SettingKey getSetting() {
        return setting;
    }

    public void setSetting(SettingKey key) {
        this.setting = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
