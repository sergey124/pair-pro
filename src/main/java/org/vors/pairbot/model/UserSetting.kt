package org.vors.pairbot.model

import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.vors.pairbot.constant.SettingKey

import javax.persistence.*

@Entity
@Table(name = "user_settings")
@EntityListeners(AuditingEntityListener::class)
@IdClass(UserSettingId::class)
class UserSetting (
        @Id @ManyToOne @JoinColumn(name = "user_pk", referencedColumnName = "pk", nullable = false)
        var user: UserInfo,

        @Id @Enumerated @Column(columnDefinition = "smallint")
        var setting: SettingKey,

        var value: String
)
