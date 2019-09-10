package org.vors.pairbot.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "teams")
public class Team {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private UUID pk;

    @OneToMany(cascade = {CascadeType.PERSIST}, mappedBy = "team")
    private Set<UserInfo> members = new HashSet<>();

    public void addMember(UserInfo user){
        getMembers().add(user);
        user.setTeam(this);
    }

    public UUID getPk() {
        return pk;
    }

    public void setPk(UUID pk) {
        this.pk = pk;
    }

    public Set<UserInfo> getMembers() {
        return members;
    }

    public void setMembers(Set<UserInfo> members) {
        this.members = members;
    }
}
