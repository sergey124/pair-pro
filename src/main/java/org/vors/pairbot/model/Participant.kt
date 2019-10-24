package org.vors.pairbot.model

import javax.persistence.*

@Entity
@Table(name = "participants")
@IdClass(ParticipantId::class)
class Participant(
        @Id @ManyToOne @JoinColumn (name = "user_pk", referencedColumnName = "pk")
        var user: UserInfo,
        @Id @ManyToOne @JoinColumn(name = "event_pk", referencedColumnName = "pk")
        var event: Event,
        var status: ParticipantStatus = ParticipantStatus.NO_RESPONSE,
        var host: Boolean = true
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Participant

                if (user != other.user) return false
                if (event != other.event) return false

                return true
        }

        override fun hashCode(): Int {
                var result = user.hashCode()
                result = 31 * result + event.hashCode()
                return result
        }
}
