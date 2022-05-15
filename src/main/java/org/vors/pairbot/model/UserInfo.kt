package org.vors.pairbot.model

import org.hibernate.annotations.CacheConcurrencyStrategy
import org.springframework.data.annotation.CreatedDate
import java.time.ZoneId
import java.util.*
import javax.persistence.*

@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "users")
class UserInfo(
        var userId: Long,
        var firstName: String,

        var lastName: String? = null,

        @Temporal(TemporalType.TIMESTAMP)
        var lastDeclineDate: Date? = null,
        var lastMessageId: Int? = null,
        var timezone: ZoneId? = null,
        @ManyToOne @JoinColumn(name = "team_pk", referencedColumnName = "pk")
        var team: Team? = null,

        @Temporal(TemporalType.TIMESTAMP) @CreatedDate
        var createdDate: Date? = null,

        @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
        var settings: MutableSet<UserSetting> = HashSet(),
        var xp: Int = 0,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var pk: Long = 0,
        @ManyToOne @JoinColumn(name = "last_partner_pk", referencedColumnName = "pk")
        var lastPartner: UserInfo? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserInfo

        if (pk != other.pk) return false

        return true
    }

    override fun hashCode(): Int {
        return pk.hashCode()
    }

    override fun toString(): String {
        return "pk $pk"
    }
}
