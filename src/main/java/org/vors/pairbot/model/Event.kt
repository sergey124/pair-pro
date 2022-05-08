package org.vors.pairbot.model

import javax.persistence.*
import java.util.Date
import java.util.HashSet

@Entity
@Table(name = "events")
class Event(
        @ManyToOne @JoinColumn(name = "creator_pk", referencedColumnName = "pk")
        var creator: UserInfo,

        @ManyToOne @JoinColumn(name = "partner_pk", referencedColumnName = "pk")
        var partner: UserInfo,
        var creatorHost: Boolean,
        var date: Date,
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var pk: Long = 0,
        @OneToMany(cascade = [CascadeType.PERSIST], mappedBy = "event")
        var participants: MutableSet<Participant> = HashSet()
) {
    init {
        participants.add(Participant(creator, this))
        participants.add(Participant(partner, this))
    }

}
