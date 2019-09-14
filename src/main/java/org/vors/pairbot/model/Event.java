package org.vors.pairbot.model;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
public class Event {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long pk;

    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "event")
    private Set<Participant> participants = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "creator_pk", referencedColumnName = "pk")
    private UserInfo creator;
    @ManyToOne
    @JoinColumn(name = "partner_pk", referencedColumnName = "pk")
    private UserInfo partner;
    @Column
    private Boolean creatorHost;

    @Column
    private Date date;
    @Column
    private Boolean accepted;

    public void addParticipant(Participant participant){
        getParticipants().add(participant);
        participant.setEvent(this);
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public Boolean isCreatorHost() {
        return creatorHost;
    }

    public void setCreatorHost(Boolean creatorHost) {
        this.creatorHost = creatorHost;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Participant> participants) {
        this.participants = participants;
    }

    public UserInfo getCreator() {
        return creator;
    }

    public void setCreator(UserInfo creator) {
        this.creator = creator;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }

    public UserInfo getPartner() {
        return partner;
    }

    public void setPartner(UserInfo partner) {
        this.partner = partner;
    }
}
