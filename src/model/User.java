package model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String role;

    public User(int id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isDoctor() {
        return "DOCTOR".equalsIgnoreCase(role);
    }

    public boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}