package org.vors.pairbot.model

import javax.persistence.*
import java.util.HashSet
import java.util.UUID

@Entity
@Table(name = "teams")
class Team (
        @Column(name = "token", columnDefinition = "BINARY(16)")
        var token: UUID,
        @ManyToOne @JoinColumn(name = "creator_pk", referencedColumnName = "pk")
        var creator: UserInfo,
        @OneToMany(mappedBy = "team")
        var members: MutableSet<UserInfo> = HashSet(),
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        var pk: Long = 0
) {
    fun addMember(user: UserInfo) {
        members.add(user)
        user.team = this
    }
}
