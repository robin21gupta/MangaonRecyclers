package com.mangaon.recycleers.model;

import com.mangaon.recycleers.enums.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    public User() {}

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId()               { return id; }
    public void setId(Long id)        { this.id = id; }
    public String getUsername()       { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getPassword()       { return password; }
    public void setPassword(String p) { this.password = p; }
    public Role getRole()             { return role; }
    public void setRole(Role role)    { this.role = role; }
}