package org.vors.pairbot.model;

public class Person {
    private int id;
    private String name;
    private boolean master;
    private boolean active = true;
    private boolean projectHolder;

    public Person(int id, String name, boolean master) {
        this.id = id;
        this.name = name;
        this.master = master;
    }

    public Person(int id, String name, boolean master, boolean active) {
        this(id, name, master);
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isProjectHolder() {
        return projectHolder;
    }

    public void setProjectHolder(boolean projectHolder) {
        this.projectHolder = projectHolder;
    }
}
