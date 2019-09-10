package org.vors.pairbot.model;

import java.io.Serializable;
import java.util.Objects;

public class ParticipantId implements Serializable {
    private Long user;
    private Long event;

    public ParticipantId() {
    }

    public ParticipantId(Long user, Long event) {
        this.user = user;
        this.event = event;
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.user, this.event);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!ParticipantId.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final ParticipantId other = (ParticipantId) obj;

        return this.getUser().equals(other.getUser()) && this.getEvent().equals(other.getEvent());
    }

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public Long getEvent() {
        return event;
    }

    public void setEvent(Long event) {
        this.event = event;
    }

}