package org.vors.pairbot.model;

import javax.persistence.*;

@Entity
@Table(name = "participants")
@IdClass(ParticipantId.class)
public class Participant {

    @Id
    @ManyToOne
    private UserInfo user;

    @Id
    @ManyToOne
    private Event event;

    @Column
    private Boolean accepted;

    @Column
    private Boolean host = false;

    public Participant(UserInfo user){
        this.user = user;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public Boolean getHost() {
        return host;
    }

    public void setHost(Boolean host) {
        this.host = host;
    }
}
