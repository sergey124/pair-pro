package org.vors.pairbot.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "users")
public class UserInfo {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long pk;

    @Column
    private Integer userId;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<UserSetting> settings = new HashSet<>();

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private Date createdDate;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastPairDate;

    @ManyToOne
    @JoinColumn(name = "team_pk")
    private Team team;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<UserSetting> getSettings() {
        return settings;
    }

    public void setSettings(Set<UserSetting> settings) {
        this.settings = settings;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Date getLastPairDate() {
        return lastPairDate;
    }

    public void setLastPairDate(Date lastPairDate) {
        this.lastPairDate = lastPairDate;
    }
}
