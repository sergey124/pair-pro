package org.vors.pairbot.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long pk;

    @Column
    private UUID token;

    @OneToMany(cascade = {CascadeType.PERSIST}, mappedBy = "team")
    private Set<UserInfo> members = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "creator_pk", referencedColumnName = "pk")
    private UserInfo creator;

    public void addMember(UserInfo user){
        getMembers().add(user);
        user.setTeam(this);
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public Set<UserInfo> getMembers() {
        return members;
    }

    public void setMembers(Set<UserInfo> members) {
        this.members = members;
    }

    public UserInfo getCreator() {
        return creator;
    }

    public void setCreator(UserInfo creator) {
        this.creator = creator;
    }

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }
}
